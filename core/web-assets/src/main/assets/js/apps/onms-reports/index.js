const angular = require('vendor/angular-js');
const _ = require('underscore');
require('../../lib/onms-http');
require('angular-ui-router');

const indexTemplate  = require('./index.html');
const onlineTemplate  = require('./online-report.html');

(function() {
    'use strict';

    var MODULE_NAME = 'onms.reports';

    angular.module(MODULE_NAME, [
            'angular-loading-bar',
            'ngResource',
            'ui.router',
            'onms.http',
        ])
        .config( ['$locationProvider', function ($locationProvider) {
            $locationProvider.hashPrefix('!');
            $locationProvider.html5Mode(false);
        }])
        .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
            $stateProvider
                .state('home', {
                    url: '/list',
                    controller: 'ReportsController',
                    templateUrl: indexTemplate
                })
                .state('online', {
                    url: '/:id/online',
                    controller: 'ReportsOnlineController',
                    templateUrl: onlineTemplate
                });
            $urlRouterProvider.otherwise('/list');
        }])
        .factory('ReportsService', function($resource) {
            return $resource('rest/reports/:id', {id: '@id'},
                {
                    'list':      { method: 'GET', isArray: true },
                    'get':       { method: 'GET' },
                }
            );
        })
        .factory('GrafanaService', function($resource) {
            return $resource('rest/endpoints/:id', {id: '@id'},
                {
                    'get':          { method: 'GET' },
                    'list':         { method: 'GET', isArray:true, params: {type: 'grafana'} },
                });
        })
        .factory('DashboardService', function($resource) {
            return $resource('rest/endpoints/grafana/:uid/dashboards', {},
                {
                    'list':         { method: 'GET', isArray:true },
                });
        })
        .controller('ReportsController', ['$scope', '$http', 'ReportsService', function($scope, $http, ReportsService) {
            $scope.refresh = function() {
                $scope.reports = [];
                $scope.globalError = undefined;

                ReportsService.list(function(response) {
                    console.log("RESULT", response);
                    if (response && Array.isArray(response)) {
                        $scope.reports = response;
                    }
                }, function(errorResponse) {
                    console.log("ERROR", errorResponse);
                    $scope.globalError = "ERROR OCCURRED :(";
                })
            };

            $scope.refresh();
        }])
        .controller('ReportsOnlineController', ['$scope', '$http', '$window', '$sce', '$stateParams', 'ReportsService', 'GrafanaService', 'DashboardService', function($scope, $http, $window, $sce, $stateParams, ReportsService, GrafanaService, DashboardService) {
            $scope.loadDetails = function() {
                $scope.loading = true;
                $scope.loading = false;
                $scope.surveillanceCategories = [];
                $scope.categories = [];
                $scope.formats = [];
                $scope.format = "PDF";
                $scope.parameters = [];
                $scope.parameters_by_name = {};
                $scope.endpoints = [];
                $scope.dashboards = [];

                ReportsService.get({id:$stateParams.id}, function(response) {
                    $scope.loading = false;
                    $scope.surveillanceCategories = response.surveillanceCategories;
                    $scope.categories = response.categories;
                    $scope.formats = response.formats;
                    $scope.parameters = response.parameters;

                    // Preprocessing
                    $scope.parameters.forEach(p => {
                        // Mark parameters which have special handling
                        if (p.name === 'GRAFANA_ENDPOINT_UID') {
                            p.hidden = true;
                        }
                        if (p.name === 'GRAFANA_DASHBOARD_UID') {
                            p.hidden = true;
                        }

                        // Index the parameters by name
                        $scope.parameters_by_name[p.name] = p;
                    });

                    // In order to have the ui look the same as before, just order the parameters
                    var order = ['string', 'integer', 'float', 'double', 'date'];
                    $scope.parameters.sort(function(left, right) {
                        return order.indexOf(left.type) - order.indexOf(right.type);
                    });

                    console.log("SUCCESS", response);
                }, function(response) {
                    $scope.loading = false;
                    console.log("ERROR", response);
                });

                GrafanaService.list(function(response) {
                    console.log("SUCCESS", response);
                    $scope.endpoints = response;
                }, function(response) {
                    console.log("ERROR", response);
                });

                $scope.$watch("selectedEndpoint", function(newValue, oldValue) {
                    console.log("Endpoint changed", newValue, oldValue);
                    if (newValue) {
                        DashboardService.list({uid: newValue}, function(response) {
                            console.log("SUCCESS", response);
                            $scope.dashboards = response;
                        }, function(response) {
                            console.log("ERROR", response);
                        })
                    }
                });
            };

            $scope.preview = function() {
                var parameters = angular.copy($scope.parameters);
                // Include the dashboard and endpoints UIDs
                parameters.push({
                    "name": "GRAFANA_ENDPOINT_UID",
                    "displayName": "endpoint uid",
                    "value": $scope.selectedEndpoint,
                    "type": "string"
                });
                parameters.push({
                    "name": "GRAFANA_DASHBOARD_UID",
                    "displayName": "dashboard uid",
                    "value": $scope.selectedDashboard,
                    "type": "string"
                });

                $http({
                    method: 'POST',
                    url: 'rest/reports/' + $stateParams.id,
                    data:  {id:$stateParams.id, parameters: parameters, format: $scope.format},
                    responseType:  'arraybuffer'
                }).then(function (response) {
                        console.log("SUCCESS", response);
                        var data = response.data;
                        var fileBlob = new Blob([data], {type: 'application/pdf'});
                        var fileURL = URL.createObjectURL(fileBlob);
                        $scope.content = $sce.trustAsResourceUrl(fileURL);

                        // var contentDisposition = response.headers("Content-Disposition");
                        // // var filename = (contentDisposition.split(';')[1].trim().split('=')[1]).replace(/"/g, '');
                        // var filename = $stateParams.id + '.pdf';
                        //
                        // var a = document.createElement('a');
                        // document.body.appendChild(a);
                        // a.style = 'display: none';
                        // a.href = fileURL;
                        // a.download = filename;
                        // a.click();
                        // window.URL.revokeObjectURL(url);
                        // document.body.removeChild(a);
                    },
                    function(error) {
                        console.log("ERROR", error);
                    });
            };

            $scope.loadDetails();
        }])
    ;
}());

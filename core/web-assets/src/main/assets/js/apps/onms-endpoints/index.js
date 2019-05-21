const angular = require('vendor/angular-js');
require('../../lib/onms-http');
require('angular-ui-router');

const indexTemplate  = require('./index.html');
const newEndpointModalTemplate = require('./new-endpoint-modal.html');

(function() {
    'use strict';

    var MODULE_NAME = 'onms.endpoints';

    angular.module(MODULE_NAME, [
        'angular-loading-bar',
        'ngResource',
        'ui.bootstrap',
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
                    controller: 'EndpointsController',
                    templateUrl: indexTemplate
                });
            $urlRouterProvider.otherwise('/list');
        }])
        .factory('EndpointsService', function($resource) {
            return $resource('rest/endpoints/:id', {id: '@id'},
                {
                    'get':          { method: 'GET'  },
                    'create':       { method: 'POST' },
                    'update':       { method: 'PUT' },
                    'list':         { method: 'GET', isArray: true },
                    'delete':       { method: 'DELETE'}
                }
            );
        })
        .controller('EndpointsController', ['$scope', '$http', '$uibModal', 'EndpointsService', function($scope, $http, $uibModal, EndpointsService) {
            $scope.refresh = function() {
                $scope.endpoints = [];
                // TODO MVR handle error
                $scope.globalError = undefined;

                EndpointsService.list(function(response) {
                    console.log("SUCCESS", response);
                    if (response && Array.isArray(response)) {
                        $scope.endpoints = response;
                        $scope.endpoints.forEach(function(item) {
                           item.revealApiKey = false;
                        });
                    }

                }, function(response) {
                    // TODO MVR handle error
                    console.log("ERROR", response);
                });
            };

            $scope.openModal = function(endpoint) {
                return $uibModal.open({
                    backdrop: false,
                    controller: 'EndpointModalController',
                    templateUrl: newEndpointModalTemplate,
                    size: 'lg',
                    resolve: {
                        endpoint: function() {
                            return endpoint;
                        }
                    }
                });
            };

            $scope.deleteEndpoint = function(deleteMe) {
                EndpointsService.delete(deleteMe, function(response) {
                    $scope.refresh();
                }, function(response) {
                    // TODO MVR handle error
                   console.log("ERROR", response);
                });
            };

            var createObject = function(endpoint) {
                return {
                    id: endpoint.id,
                    uid: endpoint.uid,
                    type: endpoint.type,
                    url: endpoint.url,
                    apiKey: endpoint.apiKey,
                    description: endpoint.description
                }
            };

            $scope.editEndpoint = function(endpoint) {
                // TODO MVR editing should actually be cancellable, etc.
                var modalInstance = $scope.openModal(endpoint);
                modalInstance.result.then(function () {
                    var clone = createObject(endpoint);
                    EndpointsService.update(clone, function() {
                        $scope.refresh();
                    }, function() {
                        // TODO MVR handle error
                        console.log("ERROR");
                    });
                }, function() {
                    // modal was dismissed
                    $scope.refresh();
                });
            };

            $scope.addNewEndpoint = function() {
                var modalInstance = $scope.openModal();
                modalInstance.result.then(function (endpoint) {
                    console.log(endpoint);
                    var clone = createObject(endpoint);
                    EndpointsService.create(clone, function(response) {
                        $scope.refresh();
                    }, function(response) {
                        // TODO MVR handle error
                        console.log("ERROR", response);
                    });
                });
            };

            $scope.refresh();
        }])
        .controller('EndpointModalController', ['$scope', '$uibModalInstance', 'endpoint', function($scope, $uibModalInstance, endpoint) {
            $scope.endpoint = endpoint || {type: 'grafana', revealApiKey: false};
            $scope.buttonName = $scope.endpoint.id ? 'Update' : 'Create';
            $scope.verifyResult = undefined;

            // var handleErrorResponse = function(response) {
            //     if (response && response.data) {
            //         var error = response.data;
            //         $scope.error = {};
            //         $scope.error[error.context] = error.message;
            //     }
            // };

            $scope.verify = function() {
                if ($scope.verifyResult && $scope.verifyResult.type !== 'danger') {
                    $scope.verifyResult = {
                        type: 'danger',
                        message: 'Nope, this does not work'
                    }
                }  else {
                    $scope.verifyResult = {
                        type: 'success',
                        message: 'Everything is awesome \\o/'
                    }
                }
            };

            $scope.save = function() {
                $uibModalInstance.close($scope.endpoint);
            };

            $scope.cancel = function() {
                $uibModalInstance.dismiss('Cancelled by User');
            };
        }])
    ;
}());

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.endpoints.rest.internal;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.netmgt.endpoints.api.EndpointType;
import org.opennms.netmgt.jasper.grafana.client.GrafanaClient;
import org.opennms.netmgt.jasper.grafana.client.GrafanaServerConfiguration;
import org.opennms.netmgt.jasper.grafana.model.Dashboard;
import org.opennms.netmgt.endpoints.api.EndpointDefinition;
import org.opennms.netmgt.endpoints.api.EndpointRepository;
import org.opennms.netmgt.endpoints.rest.EndpointRestService;

// TODO MVR add error entity
public class EndpointRestServiceImpl implements EndpointRestService {

    private final EndpointRepository endpointRepository;

    public EndpointRestServiceImpl(EndpointRepository endpointRepository) {
        this.endpointRepository = Objects.requireNonNull(endpointRepository);
    }

    @Override
    public Response listEndpoints() {
        final List<EndpointDefinition> endpoints = endpointRepository.findAll();
        if (endpoints.isEmpty()) {
            return Response.noContent().build();
        }
        final JSONArray resultArray = new JSONArray();
        endpoints.forEach(s -> resultArray.put(new JSONObject(s)));
        return Response.ok().entity(resultArray.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response listEndpoints(String type) {
        final EndpointType endpointType = EndpointType.valueOf(type); // TODO MVR implement case insensitivity
        final List<EndpointDefinition> endpoints = endpointRepository.findByType(endpointType);
        if (endpoints.isEmpty()) {
            return Response.noContent().build();
        }
        final JSONArray resultArray = new JSONArray();
        endpoints.forEach(s -> resultArray.put(new JSONObject(s)));
        return Response.ok().entity(resultArray.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response getEndpoint(String type, String endpointId) throws IOException {
        final EndpointType endpointType = EndpointType.valueOf(type); // TODO MVR implement case insensitivity
        final EndpointDefinition endpointDefinition = endpointRepository.get(endpointType, endpointId);
        final GrafanaServerConfiguration grafanaServerConfiguration = new GrafanaServerConfiguration(endpointDefinition.getUrl(), endpointDefinition.getApiKey(), 5000, 5000);
        final GrafanaClient grafanaClient = new GrafanaClient(grafanaServerConfiguration);
        final List<Dashboard> dashboards = grafanaClient.getDashboards();
        return Response.ok().entity(new JSONArray(dashboards).toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response updateEndpoint(final EndpointDefinition newEndpointDefinition) {
        endpointRepository.saveOrUpdate(newEndpointDefinition);
        return Response.accepted().build();
    }

    @Override
    public Response deleteEndpoint(final String endpointId) {
        final EndpointDefinition endpointDefinition = endpointRepository.get(endpointId);
        if (endpointDefinition == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        boolean success = endpointRepository.delete(endpointDefinition);
        if (success) {
            return Response.status(Response.Status.ACCEPTED).build();
        }
        // TODO MVR add error entity
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

}

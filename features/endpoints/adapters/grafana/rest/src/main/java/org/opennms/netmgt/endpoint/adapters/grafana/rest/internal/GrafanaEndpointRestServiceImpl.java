/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.endpoint.adapters.grafana.rest.internal;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.opennms.netmgt.endpoint.adapters.grafana.api.Dashboard;
import org.opennms.netmgt.endpoint.adapters.grafana.api.GrafanaClient;
import org.opennms.netmgt.endpoint.adapters.grafana.api.GrafanaClientFactory;
import org.opennms.netmgt.endpoint.adapters.grafana.rest.GrafanaEndpointRestService;
import org.opennms.netmgt.endpoints.api.EndpointDefinition;
import org.opennms.netmgt.endpoints.api.EndpointDefinitionRepository;

public class GrafanaEndpointRestServiceImpl implements GrafanaEndpointRestService {

    private final EndpointDefinitionRepository endpointDefinitionRepository;
    private final GrafanaClientFactory grafanaClientFactory;

    public GrafanaEndpointRestServiceImpl(final EndpointDefinitionRepository endpointDefinitionRepository, final GrafanaClientFactory grafanaClientFactory) {
        this.endpointDefinitionRepository = Objects.requireNonNull(endpointDefinitionRepository);
        this.grafanaClientFactory = Objects.requireNonNull(grafanaClientFactory);
    }

    @Override
    public Response listDashboards(final String uid) {
        final EndpointDefinition endpointDefinition = endpointDefinitionRepository.get(uid);
        final GrafanaClient client = grafanaClientFactory.createClient(endpointDefinition);
        try {
            final List<Dashboard> dashboards = client.getDashboards();
            return Response.ok().entity(new JSONArray(dashboards).toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (IOException ex) {
            // TODO MVR create error entity
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }
}

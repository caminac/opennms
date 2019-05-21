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

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.netmgt.endpoints.api.EndpointType;
import org.opennms.netmgt.endpoints.api.EndpointDefinition;
import org.opennms.netmgt.endpoints.api.EndpointDefinitionRepository;
import org.opennms.netmgt.endpoints.rest.EndpointRestService;

import com.google.common.base.Strings;

// TODO MVR add error entity
public class EndpointRestServiceImpl implements EndpointRestService {

    private final EndpointDefinitionRepository endpointDefinitionRepository;

    public EndpointRestServiceImpl(EndpointDefinitionRepository endpointDefinitionRepository) {
        this.endpointDefinitionRepository = Objects.requireNonNull(endpointDefinitionRepository);
    }

    @Override
    public Response listEndpoints(String type) {
        final List<EndpointDefinition> endpoints = getEndpoints(type);
        if (endpoints.isEmpty()) {
            return Response.noContent().build();
        }
        final JSONArray resultArray = new JSONArray();
        endpoints.forEach(s -> resultArray.put(new JSONObject(s)));
        return Response.ok().entity(resultArray.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response getEndpoint(String endpointId) {
        final EndpointDefinition endpointDefinition = endpointDefinitionRepository.get(endpointId);
        return Response.ok().entity(new JSONArray(endpointDefinition).toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response updateEndpoint(final EndpointDefinition newEndpointDefinition) {
        endpointDefinitionRepository.saveOrUpdate(newEndpointDefinition);
        return Response.accepted().build();
    }

    @Override
    public Response createEndpoint(EndpointDefinition newEndpointDefinition) {
        endpointDefinitionRepository.saveOrUpdate(newEndpointDefinition);
        return Response.accepted().build();
    }

    @Override
    public Response deleteEndpoint(final Long endpointId) {
        final EndpointDefinition endpointDefinition = endpointDefinitionRepository.get(endpointId);
        if (endpointDefinition == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        boolean success = endpointDefinitionRepository.delete(endpointDefinition);
        if (success) {
            return Response.status(Response.Status.ACCEPTED).build();
        }
        // TODO MVR add error entity
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private List<EndpointDefinition> getEndpoints(String type) {
        if (Strings.isNullOrEmpty(type)) {
            return endpointDefinitionRepository.findAll();
        }
        final EndpointType endpointType = EndpointType.parse(type);
        return endpointDefinitionRepository.findByType(endpointType);
    }

}

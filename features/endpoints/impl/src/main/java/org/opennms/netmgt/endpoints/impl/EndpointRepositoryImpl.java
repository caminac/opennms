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

package org.opennms.netmgt.endpoints.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.opennms.netmgt.endpoints.api.EndpointDefinition;
import org.opennms.netmgt.endpoints.api.EndpointRepository;
import org.opennms.netmgt.endpoints.api.EndpointType;

public class EndpointRepositoryImpl implements EndpointRepository {

    private final List<EndpointDefinition> endpoints = new ArrayList<>();

    @Override
    public List<EndpointDefinition> findAll() {
        return endpoints.stream().map(s -> new EndpointDefinition(s)).collect(Collectors.toList());
    }

    @Override
    public List<EndpointDefinition> findByType(final EndpointType endpointType) {
        Objects.requireNonNull(endpointType);
        return endpoints.stream().filter(s -> endpointType == s.getType()).map(s -> new EndpointDefinition(s)).collect(Collectors.toList());
    }

    @Override
    public EndpointDefinition get(EndpointType endpointType, String endpointId) throws NoSuchElementException {
        Objects.requireNonNull(endpointType);
        Objects.requireNonNull(endpointId);
        return endpoints.stream().filter(s -> endpointType == s.getType() && s.getId().equals(endpointId)).findAny().orElseThrow(() -> new NoSuchElementException("Could not find endpoint of type '" + endpointType + "' with id '" + endpointId + "'"));
    }

    @Override
    public EndpointDefinition get(String endpointId) {
        Objects.requireNonNull(endpointId);
        return endpoints.stream()
                .filter(s -> s.getId().equals(endpointId))
                .findAny().orElseThrow(() -> new NoSuchElementException("Could not find endpoint with id '" + endpointId + "'"));
    }

    private EndpointDefinition find(String endpointId) {
        Objects.requireNonNull(endpointId);
        return endpoints.stream()
                .filter(s -> s.getId().equals(endpointId))
                .findAny()
                .orElse(null);
    }

    @Override
    public EndpointDefinition saveOrUpdate(final EndpointDefinition endpointDefinition) {
        Objects.requireNonNull(endpointDefinition);

        // TODO MVR add validation

        if (endpointDefinition.getId() == null) {
            endpointDefinition.setId(UUID.randomUUID().toString());
        }
        final EndpointDefinition persistedEndpoint = find(endpointDefinition.getId());
        if (persistedEndpoint != null) {
            persistedEndpoint.merge(endpointDefinition);
        } else {
            endpoints.add(new EndpointDefinition(endpointDefinition));
        }
        return get(endpointDefinition.getType(), endpointDefinition.getId());
    }

    @Override
    public boolean delete(EndpointDefinition endpointDefinition) {
        Objects.requireNonNull(endpointDefinition);
        final EndpointDefinition persistedDefinition = get(endpointDefinition.getType(), endpointDefinition.getId());
        if (persistedDefinition != null) {
            return endpoints.remove(persistedDefinition);
        }
        return false;
    }
}

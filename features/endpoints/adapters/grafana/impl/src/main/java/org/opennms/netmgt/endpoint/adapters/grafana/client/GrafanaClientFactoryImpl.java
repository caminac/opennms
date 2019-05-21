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

package org.opennms.netmgt.endpoint.adapters.grafana.client;

import org.opennms.netmgt.endpoint.adapters.grafana.api.GrafanaClient;
import org.opennms.netmgt.endpoint.adapters.grafana.api.GrafanaClientFactory;
import org.opennms.netmgt.endpoints.api.EndpointDefinition;

public class GrafanaClientFactoryImpl implements GrafanaClientFactory {

    @Override
    public GrafanaClient createClient(EndpointDefinition endpointDefintion) {
        final GrafanaServerConfiguration serverConfiguration = new GrafanaServerConfiguration(endpointDefintion.getUrl(),
                endpointDefintion.getApiKey(), 5 /* TODO MVR make configurable */, 5  /* TODO MVR make configurable */);
        final GrafanaClient client = new GrafanaClientImpl(serverConfiguration);
        return client;
    }
}

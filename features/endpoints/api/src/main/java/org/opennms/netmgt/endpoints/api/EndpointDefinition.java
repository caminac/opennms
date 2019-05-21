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

package org.opennms.netmgt.endpoints.api;

import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class EndpointDefinition {

    @XmlJavaTypeAdapter(EndpointTypeAdapter.class)
    private EndpointType type;
    private Long id;
    private String uid;
    private String url;
    private String apiKey;
    private String description;

    public EndpointDefinition() {

    }

    public EndpointDefinition(EndpointDefinition definition) {
        merge(definition);
    }

    public EndpointType getType() {
        return type;
    }


    public void setType(EndpointType type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void merge(final EndpointDefinition definition) {
        Objects.requireNonNull(definition);
        setApiKey(definition.getApiKey());
        setId(definition.getId());
        setUid(definition.getUid());
        setUrl(definition.getUrl());
        setType(definition.getType());
        setDescription(definition.getDescription());
    }
}

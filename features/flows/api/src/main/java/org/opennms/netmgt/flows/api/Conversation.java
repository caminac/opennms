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

package org.opennms.netmgt.flows.api;

import java.util.Objects;
import java.util.Optional;

public class Conversation {
    private final String location;
    private final Integer protocol;
    private final Host lowerHost;
    private final Host upperHost;
    private final String application;

    private Conversation(final Builder builder) {
        this.location = Objects.requireNonNull(builder.location);
        this.protocol = Objects.requireNonNull(builder.protocol);
        this.lowerHost = new Host(builder.lowerIp, Optional.ofNullable(builder.lowerHostname));
        this.upperHost = new Host(builder.upperIp, Optional.ofNullable(builder.upperHostname));
        this.application = Objects.requireNonNull(builder.application);
    }

    public String getLocation() {
        return this.location;
    }

    public Integer getProtocol() {
        return this.protocol;
    }

    public Host getLowerHost() {
        return this.lowerHost;
    }

    public String getLowerIp() {
        return this.lowerHost.getIp();
    }

    public Optional<String> getLowerHostname() {
        return this.lowerHost.getHostname();
    }

    public Host getUpperHost() {
        return this.upperHost;
    }

    public String getUpperIp() {
        return this.upperHost.getIp();
    }

    public Optional<String> getUpperHostname() {
        return this.upperHost.getHostname();
    }

    public String getApplication() {
        return this.application;
    }

    public static class Builder {
        private String location;
        private Integer protocol;
        private String lowerIp;
        private String upperIp;
        private String lowerHostname;
        private String upperHostname;
        private String application;

        private Builder() {
        }

        public Builder withLocation(final String location) {
            this.location = Objects.requireNonNull(location);
            return this;
        }

        public Builder withProtocol(final Integer protocol) {
            this.protocol = Objects.requireNonNull(protocol);
            return this;
        }

        public Builder withLowerIp(final String lowerIp) {
            this.lowerIp = Objects.requireNonNull(lowerIp);
            return this;
        }

        public Builder withUpperIp(final String upperIp) {
            this.upperIp = Objects.requireNonNull(upperIp);
            return this;
        }

        public Builder withLowerHostname(final String hostname) {
            this.lowerHostname = hostname;
            return this;
        }

        public Builder withUpperHostname(final String hostname) {
            this.upperHostname = hostname;
            return this;
        }

        public Builder withApplication(final String application) {
            this.application = Objects.requireNonNull(application);
            return this;
        }

        public Conversation build() {
            return new Conversation(this);
        }
    }

    public static Conversation.Builder from(final ConversationKey key) {
        return new Conversation.Builder()
                .withLocation(key.getLocation())
                .withProtocol(key.getProtocol())
                .withLowerIp(key.getLowerIp())
                .withUpperIp(key.getUpperIp())
                .withApplication(key.getApplication());
    }
}

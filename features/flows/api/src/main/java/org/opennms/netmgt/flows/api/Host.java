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

public class Host {
    private final String ip;
    private final Optional<String> hostname;

    public Host(final String ip, final Optional<String> hostname) {
        this.ip = Objects.requireNonNull(ip);
        this.hostname = Objects.requireNonNull(hostname);
    }

    public Host(final Builder builder) {
        this.ip = Objects.requireNonNull(builder.ip);
        this.hostname = Optional.ofNullable(builder.hostname);
    }

    public String getIp() {
        return this.ip;
    }

    public Optional<String> getHostname() {
        return this.hostname;
    }

    public static class Builder {
        private String ip;
        private String hostname;

        private Builder() {
        }

        public Builder withIp(final String ip) {
            this.ip = Objects.requireNonNull(ip);
            return this;
        }

        public Builder withHostname(final String hostname) {
            this.hostname = Objects.requireNonNull(hostname);
            return this;
        }

        public Host build() {
            return new Host(this);
        }
    }

    public static Host.Builder from(final String ip) {
        return new Host.Builder()
                .withIp(ip);
    }
}

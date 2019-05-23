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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GrafanaServerConfiguration {
    public static final long DEFAULT_CONNECT_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);
    public static final long DEFAULT_READ_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);

    private final String url;
    private final String apiKey;
    private final long connectTimeoutMs;
    private final long readTimeoutMs;
    private final boolean strictSsl;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String url;
        private String apiKey;
        private boolean strictSsl = true;
        private long connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
        private long readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder withStrictSsl(boolean strictSsl) {
            this.strictSsl = strictSsl;
            return this;
        }

        public Builder withConnectTimeout(long duration, TimeUnit unit) {
            this.connectTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public Builder withReadTimeout(long duration, TimeUnit unit) {
            this.readTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public GrafanaServerConfiguration build() {
            Objects.requireNonNull(url, "url is required");
            Objects.requireNonNull(apiKey, "apiKey is required");
            return new GrafanaServerConfiguration(this);
        }
    }

    private GrafanaServerConfiguration(Builder builder) {
        this.url = builder.url;
        this.apiKey = builder.apiKey;
        this.strictSsl = builder.strictSsl;
        this.connectTimeoutMs = builder.connectTimeoutMs;
        this.readTimeoutMs = builder.readTimeoutMs;
    }

    public static GrafanaServerConfiguration fromEnv() {
        final File configFile = Paths.get(System.getProperty("user.home"), ".grafana", "server.properties").toFile();

        try (InputStream input = new FileInputStream(configFile)) {
            final Properties prop = new Properties();
            prop.load(input);

            // Required properties
            final String url = prop.getProperty("url");
            final String apiKey = prop.getProperty("apiKey");

            final GrafanaServerConfiguration.Builder builder = GrafanaServerConfiguration.builder()
                    .withUrl(url)
                    .withApiKey(apiKey);

            // Optional properties
            getLong(prop, "connectTimeout", timeout -> builder.withConnectTimeout(timeout, TimeUnit.SECONDS));
            getLong(prop, "readTimeout", timeout -> builder.withReadTimeout(timeout, TimeUnit.SECONDS));
            getBool(prop, "strictSsl", builder::withStrictSsl);

            return builder.build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getLong(Properties props, String key, Consumer<Long> consumer) {
        final String value = props.getProperty(key);
        if (value == null) {
            return;
        }
        try {
            final Long longValue = Long.parseLong(value);
            consumer.accept(longValue);
        } catch (NumberFormatException nfe) {
            // ignore
        }
    }

    private static void getBool(Properties props, String key, Consumer<Boolean> consumer) {
        final String value = props.getProperty(key);
        if (value == null) {
            return;
        }
        final Boolean boolValue = Boolean.parseBoolean(value);
        consumer.accept(boolValue);
    }

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isStrictSsl() {
        return strictSsl;
    }

    public long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public long getReadTimeoutMs() {
        return readTimeoutMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrafanaServerConfiguration that = (GrafanaServerConfiguration) o;
        return connectTimeoutMs == that.connectTimeoutMs &&
                readTimeoutMs == that.readTimeoutMs &&
                strictSsl == that.strictSsl &&
                Objects.equals(url, that.url) &&
                Objects.equals(apiKey, that.apiKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, apiKey, connectTimeoutMs, readTimeoutMs, strictSsl);
    }

    @Override
    public String toString() {
        return "GrafanaServerConfiguration{" +
                "url='" + url + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", connectTimeoutMs=" + connectTimeoutMs +
                ", readTimeoutMs=" + readTimeoutMs +
                ", strictSsl=" + strictSsl +
                '}';
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Opaque;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;

// typedef opaque ip_v4[4];

public class IpV4 {
    public final Opaque<byte[]> ip_v4;

    public IpV4(final ByteBuffer buffer) throws InvalidPacketException {
        this.ip_v4 = new Opaque(buffer, Optional.of(4), Opaque::parseBytes);
    }

    public IpV4(final Opaque<byte[]> ip_v4) {
        this.ip_v4 = ip_v4;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ip_v4", this.ip_v4)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter) {
        try {
            bsonWriter.writeString(Inet4Address.getByAddress(this.ip_v4.value).getHostAddress());
        } catch (UnknownHostException e) {
            Throwables.propagate(e);
        }
    }
}

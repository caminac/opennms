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

import java.nio.ByteBuffer;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

// struct extended_router {
//    next_hop nexthop;            /* IP address of next hop router */
//    unsigned int src_mask_len;   /* Source address prefix mask
//                                    (expressed as number of bits) */
//    unsigned int dst_mask_len;   /* Destination address prefix mask
//                                    (expressed as number of bits) */
// };

public class ExtendedRouter implements FlowData {
    public final NextHop nexthop;
    public final long src_mask_len;
    public final long dst_mask_len;

    public ExtendedRouter(final ByteBuffer buffer) throws InvalidPacketException {
        this.nexthop = new NextHop(buffer);
        this.src_mask_len = BufferUtils.uint32(buffer);
        this.dst_mask_len = BufferUtils.uint32(buffer);
    }

    public ExtendedRouter(final NextHop nexthop, final long src_mask_len, final long dst_mask_len) {
        this.nexthop = nexthop;
        this.src_mask_len = src_mask_len;
        this.dst_mask_len = dst_mask_len;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nexthop", this.nexthop)
                .add("src_mask_len", this.src_mask_len)
                .add("dst_mask_len", this.dst_mask_len)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("nexthop");
        this.nexthop.writeBson(bsonWriter);
        bsonWriter.writeInt64("src_mask_len", this.src_mask_len);
        bsonWriter.writeInt64("dst_mask_len", this.dst_mask_len);
        bsonWriter.writeEndDocument();
    }
}

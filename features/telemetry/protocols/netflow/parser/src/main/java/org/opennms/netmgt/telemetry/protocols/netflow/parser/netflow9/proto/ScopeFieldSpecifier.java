/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto;

import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.uint16;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.MissingTemplateException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElement;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.NullValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Field;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Scope;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

public final class ScopeFieldSpecifier implements Field, Scope {

    public static final String SCOPE_SYSTEM = "SCOPE:SYSTEM";
    public static final String SCOPE_INTERFACE = "SCOPE:INTERFACE";
    public static final String SCOPE_LINE_CARD = "SCOPE:LINE_CARD";
    public static final String SCOPE_CACHE = "SCOPE:CACHE";
    public static final String SCOPE_TEMPLATE = "SCOPE:TEMPLATE";

    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |     Scope Field Type N        |      Scope Field Length N     |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */

    public final static int SIZE = 4;

    public final int fieldType; // uint16
    public final int fieldLength; // uint16

    public final InformationElement field;

    public ScopeFieldSpecifier(final ByteBuffer buffer) throws InvalidPacketException {
        this.fieldType = uint16(buffer);
        this.fieldLength = uint16(buffer);

        this.field = from(this.fieldType)
                .orElseThrow(() -> new InvalidPacketException(buffer, "Invalid scope field type: 0x%04X", this.fieldType));

        if (this.fieldLength > this.field.getMaximumFieldLength() || this.fieldLength < this.field.getMinimumFieldLength()) {
            throw new InvalidPacketException(buffer, "Template scope field '%s' has illegal size: %d (min=%d, max=%d)",
                    this.field.getName(),
                    this.fieldLength,
                    this.field.getMinimumFieldLength(),
                    this.field.getMaximumFieldLength());
        }
    }

    @Override
    public Value<?> parse(Session.Resolver resolver, ByteBuffer buffer) throws InvalidPacketException, MissingTemplateException {
        return this.field.parse(resolver, buffer);
    }

    @Override
    public int length() {
        return this.fieldLength;
    }

    @Override
    public String getName() {
        return this.field.getName();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("scopeFieldType", this.fieldType)
                .add("scopeFieldLength", this.fieldLength)
                .toString();
    }

    private static Optional<InformationElement> from(final int fieldType) {
        switch (fieldType) {
            case 0x0001:
                return Optional.of(UnsignedValue.parserWith64Bit(SCOPE_SYSTEM, Optional.empty()));
            case 0x0002:
                return Optional.of(UnsignedValue.parserWith64Bit(SCOPE_INTERFACE, Optional.empty()));
            case 0x0003:
                return Optional.of(UnsignedValue.parserWith64Bit(SCOPE_LINE_CARD, Optional.empty()));
            case 0x0004:
                return Optional.of(UnsignedValue.parserWith64Bit(SCOPE_CACHE, Optional.empty()));
            case 0x0005:
                return Optional.of(UnsignedValue.parserWith64Bit(SCOPE_TEMPLATE, Optional.empty()));
            default:
                return Optional.empty();
        }
    }

    public static List<Value<?>> buildScopeValues(final DataRecord record) {
        final ImmutableList.Builder<Value<?>> values = ImmutableList.builder();

        values.add(new UnsignedValue(ScopeFieldSpecifier.SCOPE_SYSTEM, record.set.packet.header.sourceId));
        values.add(new UnsignedValue(ScopeFieldSpecifier.SCOPE_TEMPLATE, record.set.template.id));

        return values.build();
    }
}

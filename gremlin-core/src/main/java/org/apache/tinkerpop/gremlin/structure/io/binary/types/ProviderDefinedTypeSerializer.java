/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.structure.io.binary.types;

import java.io.IOException;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.io.Buffer;
import org.apache.tinkerpop.gremlin.structure.io.binary.DataType;
import org.apache.tinkerpop.gremlin.structure.io.binary.GraphBinaryReader;
import org.apache.tinkerpop.gremlin.structure.io.binary.GraphBinaryWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderDefinedTypeSerializer extends SimpleTypeSerializer<ProviderDefinedType> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderDefinedTypeSerializer.class);
    public ProviderDefinedTypeSerializer() {
        super(DataType.PDT);
    }

    @Override
    public ProviderDefinedType readValue(final Buffer buffer, final GraphBinaryReader context, final boolean nullable) throws IOException {
        LOGGER.info("Reading provider defined type");
        if (nullable) {
            final byte valueFlag = buffer.readByte();
            if ((valueFlag & 1) == 1) {
                return null;
            }

        }

        return readValue(buffer, context);
    }

    @Override
    protected ProviderDefinedType readValue(final Buffer buffer, final GraphBinaryReader context) throws IOException {
        String name = context.read(buffer);
        String className = context.read(buffer);
        Map<String, Object> properties = context.read(buffer);
        LOGGER.info("Read provider defined type {}", className);
        return new ProviderDefinedType(name, className, properties);
    }

    @Override
    protected void writeValue(final ProviderDefinedType pdt, final Buffer buffer, final GraphBinaryWriter context) throws IOException {
        LOGGER.info("Writing provider defined type {}", pdt.getFullyQualifiedType());
        context.write(pdt.getName(), buffer);
        context.write(pdt.getFullyQualifiedType(), buffer);
        context.write(pdt.getProperties(), buffer);
    }
}

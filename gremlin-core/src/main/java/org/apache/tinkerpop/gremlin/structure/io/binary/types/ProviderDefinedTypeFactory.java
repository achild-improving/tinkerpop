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

import java.lang.reflect.Field;

public class ProviderDefinedTypeFactory {

    private ProviderDefinedTypeFactory() {
    }

    public static <T> T toObject(ProviderDefinedType pdt, Class<T> clazz) throws Exception {
        T obj = clazz.getDeclaredConstructor().newInstance();

        pdt.getProperties().forEach((key, value) -> {
            try {
                Field field = clazz.getDeclaredField(key);
                if (value instanceof ProviderDefinedType) {
                    value = toObject((ProviderDefinedType) value, Class.forName(((ProviderDefinedType) value).getFullyQualifiedType()));
                }
                field.setAccessible(true);
                field.set(obj, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return obj;
    }

}

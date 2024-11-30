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
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ProviderDefinedType {

    private final String name;
    private final Map<String, Object> properties;
    private final String fullyQualifiedType;

    public ProviderDefinedType(Object obj) throws IOException {
        if (obj == null || !obj.getClass().isAnnotationPresent(ProviderDefined.class)) {
            throw new IllegalArgumentException("Object is required and must be ProviderDefined");
        }
        Class<?> clazz = obj.getClass();

        ProviderDefined providerDefined = clazz.getAnnotation(ProviderDefined.class);
        this.name = providerDefined.name().isEmpty() ? clazz.getSimpleName() : providerDefined.name();
        this.fullyQualifiedType = clazz.getCanonicalName();

        Set<String> include = providerDefined.includedFields().length > 0 ? new HashSet<>(Arrays.asList(providerDefined.includedFields())) : null;
        Set<String> exclude = new HashSet<>(Arrays.asList(providerDefined.excludedFields()));

        List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> !exclude.contains(f.getName())
                        && (include == null || include.contains(f.getName()))).collect(Collectors.toList());

        this.properties = new HashMap<>();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                properties.put(field.getName(), field.get(obj));
            }
        } catch (IllegalAccessException e) {
            throw new IOException(e);
        }
    }

    public ProviderDefinedType(String name, String fullyQualifiedType, Map<String, Object> properties) {
        this.name = name;
        this.fullyQualifiedType = fullyQualifiedType;
        this.properties = properties;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public String getFullyQualifiedType() {
        return this.fullyQualifiedType;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

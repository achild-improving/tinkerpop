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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.kerby.util.HexUtil;
import org.apache.tinkerpop.gremlin.structure.io.TestBuffer;
import org.apache.tinkerpop.gremlin.structure.io.binary.GraphBinaryReader;
import org.apache.tinkerpop.gremlin.structure.io.binary.GraphBinaryWriter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ProviderDefinedTypeSerializerTest {
    private final GraphBinaryWriter writer = new GraphBinaryWriter();
    private final GraphBinaryReader reader = new GraphBinaryReader();

    @Test
    public void test() throws Exception {
        TestBuffer buffer = new TestBuffer();
        writer.write(new Point(1, 2), buffer);
        outputHex(buffer);

        final ProviderDefinedType result = reader.read(buffer);
        System.out.println(result);
        assertEquals("Point", result.getName());
        assertEquals(2, result.getProperties().size());
        assertEquals(1, result.getProperties().get("x"));
        assertEquals(2, result.getProperties().get("y"));

        Point point = ProviderDefinedTypeFactory.toObject(result, Point.class);
        assertEquals(1, point.getX().intValue());
        assertEquals(2, point.getY().intValue());
    }

    @Test
    public void testNullableAttributes() throws Exception {
        TestBuffer buffer = new TestBuffer();
        writer.write(new Point(1, null), buffer);
        outputHex(buffer);

        final ProviderDefinedType result = reader.read(buffer);
        System.out.println(result);
        assertEquals("Point", result.getName());
        assertEquals(2, result.getProperties().size());
        assertEquals(1, result.getProperties().get("x"));
        assertNull(result.getProperties().get("y"));
    }

    @Test
    public void testNested() throws Exception {
        TestBuffer buffer = new TestBuffer();
        ProviderDefinedType value = new ProviderDefinedType(new Person("Andrea", 123, new Address(1234, "Main St", "shhh..."), "ABC123"));
        writer.write(value, buffer);
        outputHex(buffer);

        final ProviderDefinedType result = reader.read(buffer);
        System.out.println(result);
        assertEquals("Person", result.getName());
        assertEquals(3, result.getProperties().size());
        assertEquals("Andrea", result.getProperties().get("name"));
        assertEquals(123, result.getProperties().get("age"));
        assertTrue(result.getProperties().get("address") instanceof ProviderDefinedType);
        ProviderDefinedType address = (ProviderDefinedType) result.getProperties().get("address");
        assertEquals("addy", address.getName());
        assertEquals(2, address.getProperties().size());
        assertEquals(1234, address.getProperties().get("number"));
        assertEquals("Main St", address.getProperties().get("street"));

        Person person = ProviderDefinedTypeFactory.toObject(result, Person.class);
        System.out.println(person);

    }

    @Test
    public void testList() throws Exception {
        TestBuffer buffer = new TestBuffer();
        List<Point> list = new ArrayList<>();
        list.add(new Point(1, 2));
        writer.write(list, buffer);
        outputHex(buffer);

        final List<ProviderDefinedType> result = reader.read(buffer);
        System.out.println(result);
        assertEquals(1, result.size());
        assertEquals("Point", result.get(0).getName());
        assertEquals(2, result.get(0).getProperties().size());
        assertEquals(1, result.get(0).getProperties().get("x"));
        assertEquals(2, result.get(0).getProperties().get("y"));
    }

    @Test
    public void testSet() throws Exception {
        TestBuffer buffer = new TestBuffer();
        Set<Point> set = new HashSet<>();
        set.add(new Point(1, 2));
        writer.write(set, buffer);
        outputHex(buffer);

        final Set<ProviderDefinedType> result = reader.read(buffer);
        System.out.println(result);
        assertEquals(1, result.size());
        ProviderDefinedType pdt = result.iterator().next();
        assertEquals("Point", pdt.getName());
        assertEquals(2, pdt.getProperties().size());
        assertEquals(1, pdt.getProperties().get("x"));
        assertEquals(2, pdt.getProperties().get("y"));
    }

    @Test
    public void testMapValue() throws Exception {
        TestBuffer buffer = new TestBuffer();
        Map<String, Point> map = new HashMap<>();
        map.put("first", new Point(1, 2));
        writer.write(map, buffer);
        outputHex(buffer);

        final Map<String, ProviderDefinedType> result = reader.read(buffer);
        System.out.println(result);
        assertEquals(1, result.size());
        ProviderDefinedType pdt = result.get("first");
        assertEquals("Point", pdt.getName());
        assertEquals(2, pdt.getProperties().size());
        assertEquals(1, pdt.getProperties().get("x"));
        assertEquals(2, pdt.getProperties().get("y"));
    }

    @Test
    public void testMapKey() throws Exception {
        TestBuffer buffer = new TestBuffer();
        Map<Point, String> map = new HashMap<>();
        map.put(new Point(1, 2), "first");
        writer.write(map, buffer);
        outputHex(buffer);

        final Map<ProviderDefinedType, String> result = reader.read(buffer);
        System.out.println(result);
        assertEquals(1, result.size());
        Map.Entry<ProviderDefinedType, String> entry = result.entrySet().iterator().next();
        assertEquals("first", entry.getValue());
        ProviderDefinedType pdt = entry.getKey();
        assertEquals("Point", pdt.getName());
        assertEquals(2, pdt.getProperties().size());
        assertEquals(1, pdt.getProperties().get("x"));
        assertEquals(2, pdt.getProperties().get("y"));
    }

    @Test
    public void testFakeUnsignedInt() throws Exception {
        TestBuffer buffer = new TestBuffer();
        ProviderDefinedType value = new ProviderDefinedType(new UnsignedInt("5000000000"));
        writer.write(value, buffer);
        outputHex(buffer);

        final ProviderDefinedType result = reader.read(buffer);
        System.out.println(result);
        assertEquals("UnsignedInt", result.getName());
        assertEquals(1, result.getProperties().size());
        assertEquals("5000000000", result.getProperties().get("value"));

        UnsignedInt unsignedInt = ProviderDefinedTypeFactory.toObject(result, UnsignedInt.class);
        assertEquals("5000000000", unsignedInt.getValue());
        System.out.println(unsignedInt);
    }

    @Test
    public void testFakeUnsignedPoint() throws Exception {
        TestBuffer buffer = new TestBuffer();
        ProviderDefinedType value = new ProviderDefinedType(new UnsignedPoint(new UnsignedInt("5000000000"), new UnsignedInt("10000000000")));
        writer.write(value, buffer);
        outputHex(buffer);

        final ProviderDefinedType result = reader.read(buffer);
        System.out.println(result);
        assertEquals("UnsignedPoint", result.getName());
        assertEquals(2, result.getProperties().size());
        assertTrue(result.getProperties().get("x") instanceof ProviderDefinedType);
        assertEquals("5000000000", ((ProviderDefinedType) result.getProperties().get("x")).getProperties().get("value"));
        assertTrue(result.getProperties().get("y") instanceof ProviderDefinedType);
        assertEquals("10000000000", ((ProviderDefinedType) result.getProperties().get("y")).getProperties().get("value"));

        UnsignedPoint up = ProviderDefinedTypeFactory.toObject(result, UnsignedPoint.class);
        assertEquals("5000000000", up.getX().getValue());
        assertEquals("10000000000", up.getY().getValue());
        System.out.println(up);
    }

    private void outputHex(TestBuffer buffer) {
        byte[] b = new byte[buffer.readableBytes()];
        buffer.readerIndex(0);
        buffer.readBytes(b);
        String hex = HexUtil.bytesToHexFriendly(b);
        System.out.println("hex:" + hex);
        buffer.readerIndex(0);
    }

}
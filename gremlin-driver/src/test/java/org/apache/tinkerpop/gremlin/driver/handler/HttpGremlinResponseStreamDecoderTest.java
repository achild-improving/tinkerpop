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
package org.apache.tinkerpop.gremlin.driver.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import java.util.Collections;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tinkerpop.gremlin.util.message.ResponseMessage;
import org.apache.tinkerpop.gremlin.util.ser.SerializationException;
import org.apache.tinkerpop.gremlin.util.ser.Serializers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HttpGremlinResponseStreamDecoderTest {

    @Test
    public void shouldSucceedIfResponseSizeUnderMaxResponseContentLength() throws SerializationException {
        final String content = "this response is smaller than the max allowed";
        final FullHttpResponse httpResponse = createResponse(content);
        final EmbeddedChannel testChannel = initializeChannel(httpResponse.content().readableBytes() + 1);

        testChannel.writeInbound(httpResponse);
        final ResponseMessage inbound = testChannel.readInbound();
        assertEquals(content, inbound.getResult().getData().get(0));
    }

    @Test
    public void shouldSucceedIfResponseSizeEqualToMaxResponseContentLength() throws SerializationException {
        final String content = "this response is equal to the max allowed";
        final FullHttpResponse httpResponse = createResponse(content);
        final EmbeddedChannel testChannel = initializeChannel(httpResponse.content().readableBytes());

        testChannel.writeInbound(httpResponse);
        final ResponseMessage inbound = testChannel.readInbound();
        assertEquals(content, inbound.getResult().getData().get(0));
    }

    @Test
    public void shouldSucceedIfMaxResponseContentLengthZero() throws SerializationException {
        final String largeResponse = RandomStringUtils.random(3000);
        final FullHttpResponse httpResponse = createResponse(largeResponse);
        final EmbeddedChannel testChannel = initializeChannel(0);

        testChannel.writeInbound(httpResponse);
        final ResponseMessage inbound = testChannel.readInbound();
        assertEquals(largeResponse, inbound.getResult().getData().get(0));
    }

    @Test
    public void shouldSucceedIfResponseSizeLargerThanMaxResponseContentLength() throws SerializationException {
        final FullHttpResponse httpResponse = createResponse("this response is larger than the max allowed");
        final EmbeddedChannel testChannel = initializeChannel(httpResponse.content().readableBytes() - 1);

        try {
            testChannel.writeInbound(httpResponse);
            fail("Expected TooLongFrameException");
        } catch (TooLongFrameException e) {
            assertEquals("Response exceeded 59.0 bytes.", e.getMessage());
        }
    }

    private FullHttpResponse createResponse(String content) throws SerializationException {
        final ResponseMessage response = ResponseMessage.build().code(HttpResponseStatus.OK).result(Collections.singletonList(content)).create();
        final ByteBuf buffer = Serializers.GRAPHBINARY_V4.simpleInstance().serializeResponseAsBinary(response, ByteBufAllocator.DEFAULT);
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buffer, new DefaultHttpHeaders(), new DefaultHttpHeaders());
    }

    private EmbeddedChannel initializeChannel(final double maxResponseContentLength) {
        final HttpGremlinResponseStreamDecoder decoder = new HttpGremlinResponseStreamDecoder(Serializers.GRAPHBINARY_V4.simpleInstance(), maxResponseContentLength);
        final EmbeddedChannel testChannel = new EmbeddedChannel(new HttpServerCodec(), new HttpObjectAggregator(Integer.MAX_VALUE), decoder);
        return testChannel;
    }

}
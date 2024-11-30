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

package org.apache.tinkerpop.gremlin.structure.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class TestBuffer implements Buffer {
    private ByteBuffer buffer;

    public TestBuffer() {
        this.buffer = ByteBuffer.allocate(512);
    }

    public TestBuffer(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int readableBytes() {
        return buffer.position();
    }

    @Override
    public int readerIndex() {
        return buffer.position();
    }

    @Override
    public Buffer readerIndex(int readerIndex) {
        buffer.position(readerIndex);
        return this;
    }

    @Override
    public int writerIndex() {
        return buffer.position();
    }

    @Override
    public Buffer writerIndex(int writerIndex) {
        buffer.position(writerIndex);
        return this;
    }

    @Override
    public Buffer markWriterIndex() {
        buffer.mark();
        return this;
    }

    @Override
    public Buffer resetWriterIndex() {
        buffer.reset();
        return this;
    }

    @Override
    public int capacity() {
        return buffer.capacity();
    }

    @Override
    public boolean isDirect() {
        return buffer.isDirect();
    }

    @Override
    public boolean readBoolean() {
        return buffer.get() != 0;
    }

    @Override
    public byte readByte() {
        return buffer.get();
    }

    @Override
    public short readShort() {
        return buffer.getShort();
    }

    @Override
    public int readInt() {
        return buffer.getInt();
    }

    @Override
    public long readLong() {
        return buffer.getLong();
    }

    @Override
    public float readFloat() {
        return buffer.getFloat();
    }

    @Override
    public double readDouble() {
        return buffer.getDouble();
    }

    @Override
    public Buffer readBytes(byte[] destination) {
        buffer.get(destination);
        return this;
    }

    @Override
    public Buffer readBytes(byte[] destination, int dstIndex, int length) {
        buffer.get(destination, dstIndex, length);
        return this;
    }

    @Override
    public Buffer readBytes(ByteBuffer dst) {
        buffer.get(dst.array(), dst.position(), dst.remaining());
        return this;
    }

    @Override
    public Buffer readBytes(OutputStream out, int length) throws IOException {
        out.write(buffer.array(), buffer.position(), length);
        return this;
    }

    @Override
    public Buffer writeBoolean(boolean value) {
        buffer.put(value ? (byte) 1 : (byte) 0);
        return this;
    }

    @Override
    public Buffer writeByte(int value) {
        buffer.put((byte) value);
        return this;
    }

    @Override
    public Buffer writeShort(int value) {
        buffer.putShort((short) value);
        return this;
    }

    @Override
    public Buffer writeInt(int value) {
        buffer.putInt(value);
        return this;
    }

    @Override
    public Buffer writeLong(long value) {
        buffer.putLong(value);
        return this;
    }

    @Override
    public Buffer writeFloat(float value) {
        buffer.putFloat(value);
        return this;
    }

    @Override
    public Buffer writeDouble(double value) {
        buffer.putDouble(value);
        return this;
    }

    @Override
    public Buffer writeBytes(byte[] src) {
        buffer.put(src);
        return this;
    }

    @Override
    public Buffer writeBytes(ByteBuffer src) {
        buffer.put(src.array(), src.position(), src.remaining());
        return this;
    }

    @Override
    public Buffer writeBytes(byte[] src, int srcIndex, int length) {
        buffer.put(src, srcIndex, length);
        return this;
    }

    @Override
    public boolean release() {
        return false;
    }

    @Override
    public Buffer retain() {
        return this;
    }

    @Override
    public int referenceCount() {
        return 1;
    }

    @Override
    public int nioBufferCount() {
        return 0;
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return new ByteBuffer[0];
    }

    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        return new ByteBuffer[0];
    }

    @Override
    public ByteBuffer nioBuffer() {
        return buffer;
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return buffer;
    }

    @Override
    public Buffer getBytes(int index, byte[] dst) {
        return new TestBuffer(buffer.get(dst, dst.length, index));
    }
}

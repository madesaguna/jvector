/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.jbellis.jvector.vector;

import io.github.jbellis.jvector.vector.types.VectorFloat;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.Buffer;

/**
 * VectorFloat implementation backed by an off-heap MemorySegment.
 */
final public class OffHeapVectorFloat implements VectorFloat<MemorySegment>
{
    private final MemorySegment segment;
    private final int length;

    OffHeapVectorFloat(int length) {
        this.segment = Arena.ofAuto().allocate(length * Float.BYTES, 4);
        this.length = length;
    }

    OffHeapVectorFloat(Buffer buffer) {
        this(buffer.remaining());
        segment.copyFrom(MemorySegment.ofBuffer(buffer));
    }

    OffHeapVectorFloat(float[] data) {
        this(data.length);
        segment.copyFrom(MemorySegment.ofArray(data));
    }

    @Override
    public long ramBytesUsed()
    {
        return MemoryLayout.sequenceLayout(length, ValueLayout.JAVA_FLOAT).byteSize();
    }

    @Override
    public MemorySegment get()
    {
        return segment;
    }

    @Override
    public float get(int n)
    {
        return segment.getAtIndex(ValueLayout.JAVA_FLOAT, n);
    }

    @Override
    public void set(int n, float value)
    {
        segment.setAtIndex(ValueLayout.JAVA_FLOAT, n, value);
    }

    @Override
    public void zero() {
        segment.fill((byte) 0);
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public int offset(int i)
    {
        return i * Float.BYTES;
    }

    @Override
    public VectorFloat<MemorySegment> copy()
    {
        OffHeapVectorFloat copy = new OffHeapVectorFloat(length());
        copy.copyFrom(this, 0, 0, length());
        return copy;
    }

    @Override
    public void copyFrom(VectorFloat<?> src, int srcOffset, int destOffset, int length)
    {
        OffHeapVectorFloat csrc = (OffHeapVectorFloat) src;
        segment.asSlice((long) destOffset * Float.BYTES, (long) length * Float.BYTES)
                .copyFrom(csrc.segment.asSlice((long) srcOffset * Float.BYTES, (long) length * Float.BYTES));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < Math.min(length, 25); i++) {
            sb.append(get(i));
            if (i < length - 1) {
                sb.append(", ");
            }
        }
        if (length > 25) {
            sb.append("...");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OffHeapVectorFloat that = (OffHeapVectorFloat) o;
        return segment.mismatch(that.segment) == -1;
    }

    @Override
    public int hashCode() {
        return segment.hashCode();
    }
}

/*
 * Copyright (c) 2021 FalsePattern
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.falsepattern.jwin32.memory;

import jdk.incubator.foreign.*;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * A generic memory utility class for reading/writing into memory segments, as well as allocating on the global memory scope for long-lived variables.
 * Make sure to {@link #Free(MemorySegment)} every allocated block to avoid memory leaks, as they are not automatically released!
 */
public class MemoryUtil implements MemoryAllocator {
    static final VarHandle VH_BYTE = MemoryHandles.varHandle(byte.class, ByteOrder.nativeOrder());
    static final VarHandle VH_SHORT = MemoryHandles.varHandle(short.class, ByteOrder.nativeOrder());
    static final VarHandle VH_INT = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder());
    static final VarHandle VH_LONG = MemoryHandles.varHandle(long.class, ByteOrder.nativeOrder());
    private final Map<MemorySegment, ResourceScope> scopes = new HashMap<>();
    public static final MemoryUtil instance = new MemoryUtil();
    private MemoryUtil(){}

    /**
     * Reads a single byte from the start of the given segment.
     * @param segment The segment to read from
     * @return The value at the start of the segment
     */
    public static byte GetByte(MemorySegment segment) {
        return (byte) VH_BYTE.get(segment, 0);
    }

    /**
     * Writes a single byte to the start of the given segment
     * @param segment The segment to write to
     * @param value The value to write into the segment
     */
    public static void SetByte(MemorySegment segment, byte value) {
        VH_BYTE.set(segment, 0, value);
    }

    /**
     * Reads 2 bytes from the start of the given segment.
     * @param segment The segment to read from
     * @return The value at the start of the segment
     */
    public static short GetShort(MemorySegment segment) {
        return (short) VH_SHORT.get(segment, 0);
    }

    /**
     * Writes 2 bytes to the start of the given segment
     * @param segment The segment to write to
     * @param value The value to write into the segment
     */
    public static void SetShort(MemorySegment segment, short value) {
        VH_SHORT.set(segment, 0, value);
    }

    /**
     * Reads 4 bytes from the start of the given segment.
     * @param segment The segment to read from
     * @return The value at the start of the segment
     */
    public static int GetInt(MemorySegment segment) {
        return (int) VH_INT.get(segment, 0);
    }

    /**
     * Writes 4 bytes to the start of the given segment
     * @param segment The segment to write to
     * @param value The value to write into the segment
     */
    public static void SetInt(MemorySegment segment, int value) {
        VH_INT.set(segment, 0, value);
    }

    /**
     * Reads 8 bytes from the start of the given segment.
     * @param segment The segment to read from
     * @return The value at the start of the segment
     */
    public static long GetLong(MemorySegment segment) {
        return (long) VH_LONG.get(segment, 0);
    }

    /**
     * Writes 8 bytes to the start of the given segment
     * @param segment The segment to write to
     * @param value The value to write into the segment
     */
    public static void SetLong(MemorySegment segment, long value) {
        VH_LONG.set(segment, 0, value);
    }

    /**
     * Reads a pointer from the start of the given segment.
     * @param segment The segment to read from
     * @return The value at the start of the segment
     */
    public static MemoryAddress GetPointer(MemorySegment segment) {
        return MemoryAddress.ofLong((Long) VH_LONG.get(segment, 0));
    }

    /**
     * Writes a pointer to the start of the given segment
     * @param segment The segment to write to
     * @param value The value to write into the segment
     */
    public static void SetPointer(MemorySegment segment, MemoryAddress value) {
        VH_LONG.set(segment, 0, value.toRawLongValue());
    }

    /**
     * Releases a previously allocated memory block.
     * @param segment The segment to release
     * @throws RuntimeException When a block is freed multiple times, or when you try to free a block not allocated by MemoryUtil.
     */
    public static void Free(MemorySegment segment) {
        if (instance.scopes.containsKey(segment)) {
            instance.scopes.remove(segment).close();
        } else {
            throw new RuntimeException("Tried to free already freed or unmanaged segment!");
        }
    }

    public static MemorySegment Allocate(long bytesSize, long bytesAlignment) {
        return instance.allocate(bytesSize, bytesAlignment);
    }

    public static MemorySegment MallocAligned(MemoryLayout layout) {
        return instance.mallocAligned(layout);
    }

    public static MemorySegment Calloc(long size) {
        return instance.calloc(size);
    }

    public static MemorySegment CallocAligned(long size, long alignment) {
        return instance.callocAligned(size, alignment);
    }

    public static MemorySegment CallocAligned(MemoryLayout layout) {
        return instance.callocAligned(layout);
    }

    public static MemorySegment MallocByte() {
        return instance.mallocByte();
    }

    public static MemorySegment CallocByte() {
        return instance.callocByte();
    }

    public static MemorySegment MallocShort() {
        return instance.mallocShort();
    }

    public static MemorySegment CallocShort() {
        return instance.callocShort();
    }

    public static MemorySegment MallocInt() {
        return instance.mallocInt();
    }

    public static MemorySegment CallocInt() {
        return instance.callocInt();
    }

    public static MemorySegment MallocLong() {
        return instance.mallocLong();
    }

    public static MemorySegment CallocLong() {
        return instance.callocLong();
    }

    public static MemorySegment MallocLongLong() {
        return instance.mallocLongLong();
    }

    public static MemorySegment CallocLongLong() {
        return instance.callocLongLong();
    }

    public static MemorySegment MallocPointer() {
        return instance.mallocPointer();
    }

    public static MemorySegment CallocPointer() {
        return instance.callocPointer();
    }

    public static MemorySegment ReferenceP(MemoryAddress pValue) {
        return instance.referenceP(pValue);
    }

    public static MemorySegment ReferenceP(MemorySegment pValue) {
        return instance.referenceP(pValue);
    }

    public static MemorySegment ToCString(String str) {
        return instance.toCString(str);
    }

    public static MemorySegment Allocate(ValueLayout layout, byte value) {
        return instance.allocate(layout, value);
    }

    public static MemorySegment Allocate(ValueLayout layout, char value) {
        return instance.allocate(layout, value);
    }

    public static MemorySegment Allocate(ValueLayout layout, short value) {
        return instance.allocate(layout, value);
    }

    public static MemorySegment Allocate(ValueLayout layout, int value) {
        return instance.allocate(layout, value);
    }

    public static MemorySegment Allocate(ValueLayout layout, float value) {
        return instance.allocate(layout, value);
    }

    public static MemorySegment Allocate(ValueLayout layout, long value) {
        return instance.allocate(layout, value);
    }

    public static MemorySegment Allocate(ValueLayout layout, double value) {
        return instance.allocate(layout, value);
    }

    public static MemorySegment Allocate(ValueLayout layout, Addressable value) {
        return instance.allocate(layout, value);
    }

    public static MemorySegment AllocateArray(ValueLayout elementLayout, byte[] array) {
        return instance.allocateArray(elementLayout, array);
    }

    public static MemorySegment AllocateArray(ValueLayout elementLayout, short[] array) {
        return instance.allocateArray(elementLayout, array);
    }

    public static MemorySegment AllocateArray(ValueLayout elementLayout, char[] array) {
        return instance.allocateArray(elementLayout, array);
    }

    public static MemorySegment AllocateArray(ValueLayout elementLayout, int[] array) {
        return instance.allocateArray(elementLayout, array);
    }

    public static MemorySegment AllocateArray(ValueLayout elementLayout, float[] array) {
        return instance.allocateArray(elementLayout, array);
    }

    public static MemorySegment AllocateArray(ValueLayout elementLayout, long[] array) {
        return instance.allocateArray(elementLayout, array);
    }

    public static MemorySegment AllocateArray(ValueLayout elementLayout, double[] array) {
        return instance.allocateArray(elementLayout, array);
    }

    public static MemorySegment AllocateArray(ValueLayout elementLayout, Addressable[] array) {
        return instance.allocateArray(elementLayout, array);
    }

    public static MemorySegment Allocate(MemoryLayout layout) {
        return instance.allocate(layout);
    }

    public static MemorySegment AllocateArray(MemoryLayout elementLayout, long count) {
        return instance.allocateArray(elementLayout, count);
    }

    public static MemorySegment Allocate(long bytesSize) {
        return instance.allocate(bytesSize);
    }

    @Override
    public MemorySegment mallocAligned(long size, long alignment) {
        var scope = ResourceScope.newConfinedScope();
        var segment = MemorySegment.allocateNative(size, alignment, scope);
        scopes.put(segment, scope);
        return segment;
    }

    @Override
    public MemorySegment malloc(long size) {
        return mallocAligned(size, 1);
    }

}

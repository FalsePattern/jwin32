package com.falsepattern.jwin32.memory;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * A generic memory utility class for reading/writing into memory segments, as well as allocating on the global memory scope for long-lived variables.
 * Make sure to {@link #free(MemorySegment)} every allocated block to avoid memory leaks, as they are not automatically released!
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
    public static byte getByte(MemorySegment segment) {
        return (byte) VH_BYTE.get(segment, 0);
    }

    /**
     * Writes a single byte to the start of the given segment
     * @param segment The segment to write to
     * @param value The value to write into the segment
     */
    public static void setByte(MemorySegment segment, byte value) {
        VH_BYTE.set(segment, 0, value);
    }

    /**
     * Reads 2 bytes from the start of the given segment.
     * @param segment The segment to read from
     * @return The value at the start of the segment
     */
    public static short getShort(MemorySegment segment) {
        return (short) VH_SHORT.get(segment, 0);
    }

    /**
     * Writes 2 bytes to the start of the given segment
     * @param segment The segment to write to
     * @param value The value to write into the segment
     */
    public static void setShort(MemorySegment segment, short value) {
        VH_SHORT.set(segment, 0, value);
    }

    /**
     * Reads 4 bytes from the start of the given segment.
     * @param segment The segment to read from
     * @return The value at the start of the segment
     */
    public static int getInt(MemorySegment segment) {
        return (int) VH_INT.get(segment, 0);
    }

    /**
     * Writes 4 bytes to the start of the given segment
     * @param segment The segment to write to
     * @param value The value to write into the segment
     */
    public static void setInt(MemorySegment segment, int value) {
        VH_INT.set(segment, 0, value);
    }

    /**
     * Reads 8 bytes from the start of the given segment.
     * @param segment The segment to read from
     * @return The value at the start of the segment
     */
    public static long getLong(MemorySegment segment) {
        return (long) VH_LONG.get(segment, 0);
    }

    /**
     * Writes 8 bytes to the start of the given segment
     * @param segment The segment to write to
     * @param value The value to write into the segment
     */
    public static void setLong(MemorySegment segment, long value) {
        VH_LONG.set(segment, 0, value);
    }

    /**
     * Reads a pointer from the start of the given segment.
     * @param segment The segment to read from
     * @return The value at the start of the segment
     */
    public static MemoryAddress getPointer(MemorySegment segment) {
        return MemoryAddress.ofLong((Long) VH_LONG.get(segment, 0));
    }

    /**
     * Writes a pointer to the start of the given segment
     * @param segment The segment to write to
     * @param value The value to write into the segment
     */
    public static void setPointer(MemorySegment segment, MemoryAddress value) {
        VH_LONG.set(segment, 0, value.toRawLongValue());
    }

    /**
     * Releases a previously allocated memory block.
     * @param segment The segment to release
     * @throws RuntimeException When a block is freed multiple times, or when you try to free a block not allocated by MemoryUtil.
     */
    public void free(MemorySegment segment) {
        if (scopes.containsKey(segment)) {
            scopes.remove(segment).close();
        } else {
            throw new RuntimeException("Tried to free already freed or unmanaged segment!");
        }
    }

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

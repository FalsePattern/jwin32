package com.falsepattern.jwin32.memory;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class MemoryUtil implements MemoryAllocator {
    static final VarHandle VH_BYTE = MemoryHandles.varHandle(byte.class, ByteOrder.nativeOrder());
    static final VarHandle VH_SHORT = MemoryHandles.varHandle(short.class, ByteOrder.nativeOrder());
    static final VarHandle VH_INT = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder());
    static final VarHandle VH_LONG = MemoryHandles.varHandle(long.class, ByteOrder.nativeOrder());
    private final Map<MemorySegment, ResourceScope> scopes = new HashMap<>();
    public static final MemoryUtil instance = new MemoryUtil();
    private MemoryUtil(){}

    public static byte getByte(MemorySegment segment) {
        return (byte) VH_BYTE.get(segment, 0);
    }

    public static void setByte(MemorySegment segment, byte value) {
        VH_BYTE.set(segment, 0, value);
    }

    public static short getShort(MemorySegment segment) {
        return (short) VH_SHORT.get(segment, 0);
    }

    public static void setShort(MemorySegment segment, short value) {
        VH_SHORT.set(segment, 0, value);
    }

    public static int getInt(MemorySegment segment) {
        return (int) VH_INT.get(segment, 0);
    }

    public static void setInt(MemorySegment segment, int value) {
        VH_INT.set(segment, 0, value);
    }

    public static long getLong(MemorySegment segment) {
        return (long) VH_LONG.get(segment, 0);
    }

    public static void setLong(MemorySegment segment, long value) {
        VH_LONG.set(segment, 0, value);
    }

    public static long getPointer(MemorySegment segment) {
        return (long) VH_LONG.get(segment, 0);
    }

    public static void setPointer(MemorySegment segment, long value) {
        VH_LONG.set(segment, 0, value);
    }

    public static long addressOf(MemorySegment segment) {
        return segment.address().segmentOffset(MemorySegment.globalNativeSegment());
    }

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

    public static MemoryAddress dereferencePointer(MemorySegment ppValue) {
        return MemoryAddress.ofLong(MemoryUtil.getPointer(ppValue));
    }
}

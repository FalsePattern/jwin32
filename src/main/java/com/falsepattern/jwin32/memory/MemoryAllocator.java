package com.falsepattern.jwin32.memory;

import jdk.incubator.foreign.*;

public interface MemoryAllocator extends SegmentAllocator {

    MemorySegment mallocAligned(long size, long alignment);
    MemorySegment malloc(long size);


    private static MemorySegment zero(MemorySegment segment) {
        segment.fill((byte)0);
        return segment;
    }

    @Override
    default MemorySegment allocate(long bytesSize, long bytesAlignment) {
        return mallocAligned(bytesSize, bytesAlignment);
    }

    default MemorySegment mallocAligned(MemoryLayout layout) {
        return mallocAligned(layout.byteSize(), layout.byteAlignment());
    }

    default MemorySegment calloc(long size) {
        return zero(malloc(size));
    }

    default MemorySegment callocAligned(long size, long alignment) {
        return zero(mallocAligned(size, alignment));
    }

    default MemorySegment callocAligned(MemoryLayout layout) {
        return callocAligned(layout.byteSize(), layout.byteAlignment());
    }

    default MemorySegment mallocByte() {
        return mallocAligned(1, 1);
    }

    default MemorySegment callocByte() {
        return zero(mallocByte());
    }

    default MemorySegment mallocShort() {
        return mallocAligned(CLinker.C_SHORT.byteSize(), CLinker.C_SHORT.byteAlignment());
    }

    default MemorySegment callocShort() {
        return zero(mallocShort());
    }

    default MemorySegment mallocInt() {
        return mallocAligned(CLinker.C_INT.byteSize(), CLinker.C_INT.byteAlignment());
    }

    default MemorySegment callocInt() {
        return zero(mallocInt());
    }

    default MemorySegment mallocLong() {
        return mallocAligned(CLinker.C_LONG.byteSize(), CLinker.C_LONG.byteAlignment());
    }

    default MemorySegment callocLong() {
        return zero(mallocLong());
    }

    default MemorySegment mallocPointer() {
        return mallocAligned(CLinker.C_POINTER.byteSize(), CLinker.C_POINTER.byteAlignment());
    }

    default MemorySegment callocPointer() {
        return zero(mallocPointer());
    }

    default MemorySegment referenceP(MemoryAddress pValue) {
        var ppValue = mallocPointer();
        MemoryUtil.setPointer(ppValue, pValue.segmentOffset(MemorySegment.globalNativeSegment()));
        return ppValue;
    }

    default MemorySegment referenceP(MemorySegment pValue) {
        return referenceP(pValue.address());
    }

    default MemorySegment toCString(String str) {
        return CLinker.toCString(str, this);
    }
}

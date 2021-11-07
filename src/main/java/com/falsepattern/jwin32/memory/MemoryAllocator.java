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

/**
 * A memory allocator handles creation of fixed-size memory segments. De-allocation support is not compulsive,
 * one such example is the {@link MemoryStack}, which releases allocated segments in bulk when the current scope is
 * popped.
 */
public interface MemoryAllocator extends SegmentAllocator {

    /**
     * Allocates a block of memory aligned to a specific byte offset.
     * @param size The total amount of bytes to allocate
     * @param alignment The byte alignment of the start of the segment
     * @return A memory segment allocated with the specific alignment and size
     */
    MemorySegment mallocAligned(long size, long alignment);

    /**
     * Allocates a block of memory without any alignment. Equivalent to {@link #mallocAligned}<code>(size, 1)</code>.
     * @param size The total amount of bytes to allocate
     * @return An unaligned memory segment with the specific size
     */
    MemorySegment malloc(long size);

    /**
     * Fills an entire memory segment with zeroes.
     * @param segment The segment to fill with zeroes
     * @return The input segment for chaining
     */
    private static MemorySegment zero(MemorySegment segment) {
        segment.fill((byte)0);
        return segment;
    }

    @Override
    default MemorySegment allocate(long bytesSize, long bytesAlignment) {
        return mallocAligned(bytesSize, bytesAlignment);
    }

    /**
     * Allocates a memory segment that meets the given memory layout's alignment and size constraints.
     * @param layout The memory layout to allocate based on
     * @return A memory segment that matches the layout's constraints
     */
    default MemorySegment mallocAligned(MemoryLayout layout) {
        return mallocAligned(layout.byteSize(), layout.byteAlignment());
    }

    /**
     * Same as {@link #malloc(long)}, but initializes every byte of the allocated segment to zero.
     * @param size The total amount of bytes to allocate
     * @return An unaligned memory segment with the specific size, initialized to zero
     */
    default MemorySegment calloc(long size) {
        return zero(malloc(size));
    }

    /**
     * Same as {@link #mallocAligned(long, long)}, but initializes every byte of the allocated segment to zero.
     * @param size The total amount of bytes to allocate
     * @param alignment The byte alignment of the start of the segment
     * @return A memory segment allocated with the specific alignment and size, initialized to zero
     */
    default MemorySegment callocAligned(long size, long alignment) {
        return zero(mallocAligned(size, alignment));
    }

    /**
     * Same as {@link #mallocAligned(long, long)}, but initializes every byte of the allocated segment to zero.
     * @param layout The memory layout to allocate based on
     * @return A memory segment that matches the layout's constraints, initialized to zero
     */
    default MemorySegment callocAligned(MemoryLayout layout) {
        return callocAligned(layout.byteSize(), layout.byteAlignment());
    }

    /**
     * Allocates a single byte of memory
     * @return A memory segment with size 1 and alignment 1
     */
    default MemorySegment mallocByte() {
        return mallocAligned(1, 1);
    }

    /**
     * Allocates a single byte of memory, and initializes it to zero
     * @return A memory segment with size 1 and alignment 1, initialized to zero
     */
    default MemorySegment callocByte() {
        return zero(mallocByte());
    }

    /**
     * Allocates 2 bytes of memory
     * @return A memory segment with size 2 and alignment 2
     */
    default MemorySegment mallocShort() {
        return mallocAligned(CLinker.C_SHORT.byteSize(), CLinker.C_SHORT.byteAlignment());
    }

    /**
     * Allocates 2 bytes of memory, and initializes it to zero
     * @return A memory segment with size 2 and alignment 2, initialized to zero
     */
    default MemorySegment callocShort() {
        return zero(mallocShort());
    }

    /**
     * Allocates 4 bytes of memory
     * @return A memory segment with size 4 and alignment 4
     */
    default MemorySegment mallocInt() {
        return mallocAligned(CLinker.C_INT.byteSize(), CLinker.C_INT.byteAlignment());
    }

    /**
     * Allocates 4 bytes of memory, and initializes it to zero
     * @return A memory segment with size 4 and alignment 4, initialized to zero
     */
    default MemorySegment callocInt() {
        return zero(mallocInt());
    }

    /**
     * Allocates 4/8 bytes of memory, depending on the operating system (windows: 4 bytes, SystemV/AArch64: 8 bytes)
     * @return A memory segment with size 4/8 and alignment 4/8, depending on the operating system
     */
    default MemorySegment mallocLong() {
        return mallocAligned(CLinker.C_LONG.byteSize(), CLinker.C_LONG.byteAlignment());
    }

    /**
     * Allocates 4/8 bytes of memory, initialized to zero, depending on the operating system (windows: 4 bytes, SystemV/AArch64: 8 bytes)
     * @return A memory segment with size 4/8 and alignment 4/8, initialized to zero, depending on the operating system
     */
    default MemorySegment callocLong() {
        return zero(mallocLong());
    }

    /**
     * Allocates 8 bytes of memory
     * @return A memory segment with size 8 and alignment 8
     */
    default MemorySegment mallocLongLong() {
        return mallocAligned(CLinker.C_LONG_LONG.byteSize(), CLinker.C_LONG_LONG.byteAlignment());
    }

    /**
     * Allocates 8 bytes of memory, and initializes it to zero
     * @return A memory segment with size 8 and alignment 8, initialized to zero
     */
    default MemorySegment callocLongLong() {
        return zero(mallocLongLong());
    }

    /**
     * Allocates 8 bytes of memory
     * @return A memory segment with size 8 and alignment 8
     */
    default MemorySegment mallocPointer() {
        return mallocAligned(CLinker.C_POINTER.byteSize(), CLinker.C_POINTER.byteAlignment());
    }

    /**
     * Allocates 8 bytes of memory, and initializes it to zero
     * @return A memory segment with size 8 and alignment 8, initialized to zero
     */
    default MemorySegment callocPointer() {
        return zero(mallocPointer());
    }

    /**
     * Allocates 8 bytes of memory, then inserts the given address into it as a pointer (*int -> **int, for example)
     * @param pValue A pointer to some memory
     * @return A pointer to the pointer
     */
    default MemorySegment referenceP(MemoryAddress pValue) {
        var ppValue = mallocPointer();
        MemoryUtil.SetPointer(ppValue, pValue);
        return ppValue;
    }

    /**
     * Same as {@link #referenceP(MemoryAddress)}, but for {@link MemorySegment}s.
     * @param pValue A pointer to some memory
     * @return A pointer to the pointer
     */
    default MemorySegment referenceP(MemorySegment pValue) {
        return referenceP(pValue.address());
    }

    /**
     * Converts a java string to a native string using the platform charset.
     * @param str The string to convert to native
     * @return The native copy of the string
     */
    default MemorySegment toCString(String str) {
        return CLinker.toCString(str, this);
    }
}

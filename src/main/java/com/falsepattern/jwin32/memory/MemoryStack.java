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

import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

import java.util.Stack;

/**
 * A thread local allocated memory segment, which is useful for allocating short-lived native memory, such as configuration structs.
 */
public class MemoryStack implements MemoryAllocator, AutoCloseable {
    private static final long MAX_ALIGNMENT = 0x1000;

    private final MemorySegment rootSegment;
    @SuppressWarnings("FieldCanBeLocal")
    private final ResourceScope scope;
    private final Stack<Long> offsets = new Stack<>();
    private final Stack<ResourceScope> scopes = new Stack<>();

    private ResourceScope currentScope;
    private long baseOffset = 0;
    private long currentOffset = 0;

    private static final ThreadLocal<MemoryStack> threadLocalStack = ThreadLocal.withInitial(() -> {
        var scope = ResourceScope.newImplicitScope();
        //4 MB thread local stack
        var segment = MemorySegment.allocateNative(4 * 1024 * 1024, MAX_ALIGNMENT, scope);
        return new MemoryStack(segment, scope);
    });

    private MemoryStack(MemorySegment rootSegment, ResourceScope scope) {
        this.scope = scope;
        this.rootSegment = rootSegment;
        this.currentScope = ResourceScope.newConfinedScope();
    }

    private void align(long alignment) {
        if (currentOffset % alignment != 0)
            currentOffset += alignment - (currentOffset % alignment);
    }

    @Override
    public MemorySegment mallocAligned(long size, long alignment) {
        if (alignment > MAX_ALIGNMENT) throw new IllegalArgumentException("Tried to allocate with alignment " + alignment + ", which is greater than the maximum " + MAX_ALIGNMENT);
        align(alignment);
        return malloc(size);
    }

    @Override
    public MemorySegment malloc(long size) {
        var newSegment = rootSegment.asSlice(currentOffset, size);
        currentOffset += size;
        return newSegment;
    }

    /**
     * Retrieves the thread local stack without pushing a new memory scope.
     * @return The thread's stack
     */
    public static MemoryStack getCurrentStack() {
        return threadLocalStack.get();
    }

    /**
     * Pushes a new memory scope, which then later must be {@link #pop()}-ed.
     * @return This memory stack
     */
    public MemoryStack push() {
        offsets.push(baseOffset);
        scopes.push(currentScope);
        currentScope = ResourceScope.newConfinedScope();
        baseOffset = currentOffset;
        return this;
    }

    /**
     * Pops the topmost memory scope, releasing all segments allocated in that scope.
     */
    public void pop() {
        if (offsets.isEmpty()) throw new RuntimeException("Tried to pop empty memory stack!");
        currentOffset = baseOffset;
        currentScope.close();
        currentScope = scopes.pop();
        baseOffset = offsets.pop();
    }

    /**
     * Retrieves the thread local stack, and then immediately pushes a new memory scope.
     * Useful for try-with-resource blocks.
     * @return The thread's memory stack
     */
    public static MemoryStack stackPush() {
        var stack = threadLocalStack.get();
        return stack.push();
    }

    /**
     * Used for try-with-resources blocks, it's a wrapper function for {@link #pop()}.
     */
    @Override
    public void close() {
        pop();
    }

    /**
     * @return The currently active memory scope for functions that require one.
     */
    public ResourceScope scope() {
        return currentScope;
    }
}

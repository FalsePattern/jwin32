package com.falsepattern.jwin32.memory;

import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

import java.util.Stack;

public class MemoryStack implements MemoryAllocator, AutoCloseable {
    private static final long MAX_ALIGNMENT = 0x1000;

    private final MemorySegment rootSegment;
    private final ResourceScope scope;
    private final Stack<Long> offsets = new Stack<>();
    private final Stack<ResourceScope> scopes = new Stack<>();

    private ResourceScope currentScope;
    private long baseOffset = 0;
    private long currentOffset = 0;

    private static final ThreadLocal<MemoryStack> threadLocalStack = ThreadLocal.withInitial(() -> {
        var scope = ResourceScope.newImplicitScope();
        //4MB thread local stack
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

    public static MemoryStack getCurrentStack() {
        return threadLocalStack.get();
    }

    public MemoryStack push() {
        offsets.push(baseOffset);
        scopes.push(currentScope);
        currentScope = ResourceScope.newConfinedScope();
        baseOffset = currentOffset;
        return this;
    }

    public void pop() {
        if (offsets.isEmpty()) throw new RuntimeException("Tried to pop empty memory stack!");
        currentOffset = baseOffset;
        currentScope.close();
        currentScope = scopes.pop();
        baseOffset = offsets.pop();
    }

    public static MemoryStack stackPush() {
        var stack = threadLocalStack.get();
        return stack.push();
    }

    @Override
    public void close() {
        pop();
    }

    public void destroy() {
        currentScope.close();
        while (scopes.size() > 0) {
            scopes.pop().close();
        }
        scope.close();
    }

    public ResourceScope scope() {
        return scope;
    }
}

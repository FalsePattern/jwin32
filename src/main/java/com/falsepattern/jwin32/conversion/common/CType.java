package com.falsepattern.jwin32.conversion.common;

import jdk.incubator.foreign.*;

public record CType(String name, String simpleName, boolean primitive) {
    public static final CType VOID = new CType(void.class);
    public static final CType BYTE = new CType(byte.class);
    public static final CType CHAR = new CType(char.class);
    public static final CType SHORT = new CType(short.class);
    public static final CType INT = new CType(int.class);
    public static final CType LONG = new CType(long.class);
    public static final CType MEMORY_SEGMENT = new CType(MemorySegment.class);
    public static final CType MEMORY_ADDRESS = new CType(MemoryAddress.class);
    public static final CType SEGMENT_ALLOCATOR = new CType(SegmentAllocator.class);
    public CType(Class<?> clazz) {
        this(clazz.getName().replace('$', '.'), getSimpleString(clazz), clazz.isPrimitive());
    }

    private static String getSimpleString(Class<?> clazz) {
        var enclosing = clazz.getEnclosingClass();
        if (enclosing != null) {
            return getSimpleString(enclosing) + "." + clazz.getSimpleName();
        } else {
            return clazz.getSimpleName();
        }
    }

    public String asImport() {
        return primitive ? "" : "import %s;\n".formatted(name);
    }

    public static CType fromValueLayout(ValueLayout layout) {
        return switch ((int) layout.byteSize()) {
            case 1 -> BYTE;
            case 2 -> SHORT;
            case 4 -> INT;
            case 8 -> LONG;
            default -> null;
        };
    }
}

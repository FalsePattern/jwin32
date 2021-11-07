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
package com.falsepattern.jwin32.internal.conversion.common;

import jdk.incubator.foreign.*;
import win32.pure.Win32;

public record CType(String name, String simpleName, boolean primitive) {
    public static final CType VOID = new CType(void.class);
    public static final CType BYTE = new CType(byte.class);
    public static final CType CHAR = new CType(char.class);
    public static final CType SHORT = new CType(short.class);
    public static final CType INT = new CType(int.class);
    public static final CType LONG = new CType(long.class);
    public static final CType FLOAT = new CType(float.class);
    public static final CType DOUBLE = new CType(double.class);
    public static final CType STRING = new CType(String.class);
    public static final CType MEMORY_SEGMENT = new CType(MemorySegment.class);
    public static final CType MEMORY_ADDRESS = new CType(MemoryAddress.class);
    public static final CType SEGMENT_ALLOCATOR = new CType(SegmentAllocator.class);
    public static final CType RESOURCE_SCOPE = new CType(ResourceScope.class);
    public static final CType WIN32 = new CType(Win32.class);
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

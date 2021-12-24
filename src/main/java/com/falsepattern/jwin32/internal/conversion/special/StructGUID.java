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

package com.falsepattern.jwin32.internal.conversion.special;

import com.falsepattern.jwin32.internal.conversion.common.*;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;

import java.util.ArrayList;
import java.util.List;

import static jdk.incubator.foreign.CLinker.C_CHAR;

/**
 * Special conversion for the GUID structure.
 */
public class StructGUID implements SpecialBehaviour {

    private final List<Class<?>> BASE_IMPLS = new ArrayList<>();



    @Override
    public boolean isApplicableBase(GroupLayout layout, String pkg, Class<?> parent) {
        if (parent.getName().contains("GUID")) { //Early bailout before checking member layouts
            var members = layout.memberLayouts();
            return members.get(0).equals(CLinker.C_LONG.withName("Data1")) &&
                    members.get(1).equals(CLinker.C_SHORT.withName("Data2")) &&
                    members.get(2).equals(CLinker.C_SHORT.withName("Data3")) &&
                    members.get(3).equals(MemoryLayout.sequenceLayout(8, C_CHAR).withName("Data4"));
        }
        return false;
    }

    @Override
    public boolean isApplicableExtends(String pkg, Class<?> parent, Class<?> baseImpl) {
        return BASE_IMPLS.contains(baseImpl);
    }


    public void applyBaseImpl(CClass implementation, GroupLayout layout, String pkg, Class<?> parent) {
        BASE_IMPLS.add(parent);
        CConstructor ctor;

        //Raw versions

        //Segment version
        ctor = new CConstructor();
        ctor.accessSpecifier.vis = AccessSpecifier.Visibility.PUBLIC;
        ctor.paramList.add(new CParameter(CType.MEMORY_SEGMENT, "segment"));
        addArgs(ctor);
        ctor.code.append("this(segment);\nData1(data1);\nData2(data2);\nData3(data3);\n").append(parent.getSimpleName()).append(".Data4$slice(segment).copyFrom(MemorySegment.ofArray(data4));\n");
        implementation.addConstructor(ctor);

        //Allocator version
        ctor = new CConstructor();
        ctor.accessSpecifier.vis = AccessSpecifier.Visibility.PUBLIC;
        ctor.paramList.add(new CParameter(CType.SEGMENT_ALLOCATOR, "allocator"));
        addArgs(ctor);
        ctor.code.append("this(").append(parent.getSimpleName()).append(".allocate(allocator), data1, data2, data3, data4);\n");
        implementation.addConstructor(ctor);

        //String versions

        //Segment version
        ctor = new CConstructor();
        ctor.accessSpecifier.vis = AccessSpecifier.Visibility.PUBLIC;
        ctor.paramList.add(new CParameter(CType.MEMORY_SEGMENT, "segment"));
        ctor.paramList.add(new CParameter(CType.STRING, "uuid"));
        ctor.code.append("this(segment, ")
                .append(pInt(0, 8)).append(", ")
                .append(pShort(9, 13)).append(", ")
                .append(pShort(14, 18)).append(", ")
                .append("new byte[] {")
                    .append(pByte(19, 21)).append(", ")
                    .append(pByte(21, 23)).append(", ")
                    .append(pByte(24, 26)).append(", ")
                    .append(pByte(26, 28)).append(", ")
                    .append(pByte(28, 30)).append(", ")
                    .append(pByte(30, 32)).append(", ")
                    .append(pByte(32, 34)).append(", ")
                    .append(pByte(34, 36)).append("});\n");
        implementation.addConstructor(ctor);

        //Allocator version
        ctor = new CConstructor();
        ctor.accessSpecifier.vis = AccessSpecifier.Visibility.PUBLIC;
        ctor.paramList.add(new CParameter(CType.SEGMENT_ALLOCATOR, "allocator"));
        ctor.paramList.add(new CParameter(CType.STRING, "uuid"));
        ctor.code.append("this(").append(parent.getSimpleName()).append(".allocate(allocator), uuid);\n");
        implementation.addConstructor(ctor);
    }

    public void applyExtendsImpl(CClass implementation, String pkg, Class<?> parent, Class<?> baseImpl) {

        CConstructor ctor;

        //Raw versions

        //Segment version
        ctor = new CConstructor();
        ctor.accessSpecifier.vis = AccessSpecifier.Visibility.PUBLIC;
        ctor.paramList.add(new CParameter(CType.MEMORY_SEGMENT, "segment"));
        addArgs(ctor);
        ctor.code.append("super(segment, data1, data2, data3, data4);\n");
        implementation.addConstructor(ctor);

        //Allocator version
        ctor = new CConstructor();
        ctor.accessSpecifier.vis = AccessSpecifier.Visibility.PUBLIC;
        ctor.paramList.add(new CParameter(CType.SEGMENT_ALLOCATOR, "allocator"));
        addArgs(ctor);
        ctor.code.append("super(allocator, data1, data2, data3, data4);\n");
        implementation.addConstructor(ctor);

        //String versions

        //Segment version
        ctor = new CConstructor();
        ctor.accessSpecifier.vis = AccessSpecifier.Visibility.PUBLIC;
        ctor.paramList.add(new CParameter(CType.MEMORY_SEGMENT, "segment"));
        ctor.paramList.add(new CParameter(CType.STRING, "uuid"));
        ctor.code.append("super(segment, uuid);\n");
        implementation.addConstructor(ctor);

        //Allocator version
        ctor = new CConstructor();
        ctor.accessSpecifier.vis = AccessSpecifier.Visibility.PUBLIC;
        ctor.paramList.add(new CParameter(CType.SEGMENT_ALLOCATOR, "allocator"));
        ctor.paramList.add(new CParameter(CType.STRING, "uuid"));
        ctor.code.append("super(allocator, uuid);\n");
        implementation.addConstructor(ctor);
    }

    private static String pInt(int start, int end) {
        return "Integer.parseUnsignedInt(uuid.substring(" + start + ", " + end + "), 16)";
    }

    private static String pShort(int start, int end) {
        return "((short)" + pInt(start, end) + ")";
    }

    private static String pByte(int start, int end) {
        return "((byte)" + pInt(start, end) + ")";
    }

    private static void addArgs(CConstructor ctor) {
        ctor.paramList.add(new CParameter(CType.INT, "data1"));
        ctor.paramList.add(new CParameter(CType.SHORT, "data2"));
        ctor.paramList.add(new CParameter(CType.SHORT, "data3"));
        ctor.paramList.add(new CParameter(new CType(byte[].class), "data4"));
    }
}

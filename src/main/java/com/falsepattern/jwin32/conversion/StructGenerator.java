package com.falsepattern.jwin32.conversion;

import com.falsepattern.jwin32.conversion.common.*;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SegmentAllocator;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class StructGenerator {
    private static final CType MEMORY_SEGMENT = new CType(MemorySegment.class);
    private static final CType SEGMENT_ALLOCATOR = new CType(SegmentAllocator.class);
    private static final CParameter ALLOCATOR_PARAM = new CParameter(SEGMENT_ALLOCATOR, "allocator");
    private static final CParameter SEGMENT_PARAM = new CParameter(MEMORY_SEGMENT, "segment");
    private static final CField SEGMENT = new CField();
    private static final CConstructor SEGMENT_CONSTRUCTOR = new CConstructor();
    static {
        SEGMENT.accessSpecifier.pub = true;
        SEGMENT.accessSpecifier.fin = true;
        SEGMENT.type = MEMORY_SEGMENT;
        SEGMENT.name = "segment";

        SEGMENT_CONSTRUCTOR.accessSpecifier.pub = true;
        SEGMENT_CONSTRUCTOR.paramList.add(SEGMENT_PARAM);
        SEGMENT_CONSTRUCTOR.code.append("this.segment = segment;");
    }

    public static CClass generateStruct(String pkg, Class<?> baseClass) {
        var struct = new CClass();
        struct.name = baseClass.getSimpleName() + "_J";
        struct.pkg = pkg;
        struct.accessSpecifier.pub = true;
        struct.importImplicitly(new CType(baseClass));
        struct.addField(SEGMENT);
        struct.addConstructor(SEGMENT_CONSTRUCTOR);
        struct.addConstructor(getAllocatorConstructor(baseClass));
        Arrays.stream(baseClass.getMethods())
                .sorted((a, b) -> {
                    if (!a.getName().equals(b.getName())) {
                        return a.getName().compareTo(b.getName());
                    } else if (!a.getReturnType().equals(b.getReturnType())) {
                        var art = a.getReturnType();
                        var brt = b.getReturnType();
                        if (art.equals(void.class)) return -1;
                        if (brt.equals(void.class)) return 1;
                        if (art.isPrimitive() && !brt.isPrimitive()) return -1;
                        if (!art.isPrimitive() && brt.isPrimitive()) return 1;
                        return art.getName().compareTo(brt.getName());
                    } else {
                        return 0;
                    }
                })
                .map(method -> parseMethod(baseClass, method))
                .filter(Objects::nonNull)
                .forEach(struct::addMethod);
        return struct;
    }

    private static CConstructor getAllocatorConstructor(Class<?> baseClass) {
        var constructor = new CConstructor();
        constructor.accessSpecifier.pub = true;
        constructor.paramList.add(ALLOCATOR_PARAM);
        constructor.code.append("segment = ").append(baseClass.getSimpleName()).append(".allocate(allocator);");
        return constructor;
    }

    private static CMethod parseMethod(Class<?> owner, Method method) {
        var name = method.getName();
        if (name.endsWith("$set") && method.getParameterCount() == 2) {
            var result = new CMethod();
            result.accessSpecifier.pub = true;
            result.returnType = new CType(void.class);
            result.name = name.substring(0, name.indexOf('$'));
            result.paramList.add(new CParameter(new CType(method.getParameters()[1].getType()), "value"));
            result.code.append(owner.getSimpleName()).append(".").append(name).append("(segment, value);");
            return result;
        } else if (name.endsWith("$get") && method.getParameterCount() == 1) {
            var result = new CMethod();
            result.accessSpecifier.pub = true;
            result.returnType = new CType(method.getReturnType());
            result.name = name.substring(0, name.indexOf('$'));
            result.code.append("return ").append(owner.getSimpleName()).append(".").append(name).append("(segment);");
            return result;
        } else {
            return null;
        }

    }
}

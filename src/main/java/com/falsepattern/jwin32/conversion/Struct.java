package com.falsepattern.jwin32.conversion;

import com.falsepattern.jwin32.conversion.common.*;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.ValueLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class Struct {
    private static final CField SEGMENT_FIELD = new CField();
    private static final CConstructor SUPER_CALL_ASSIGN = new CConstructor();
    private static final CConstructor SUPER_CALL_ALLOCATOR = new CConstructor();
    static {
        SEGMENT_FIELD.accessSpecifier.pub = true;
        SEGMENT_FIELD.accessSpecifier.fin = true;
        SEGMENT_FIELD.name = "segment";
        SEGMENT_FIELD.type = CType.MEMORY_SEGMENT;

        SUPER_CALL_ALLOCATOR.accessSpecifier.pub = true;
        SUPER_CALL_ALLOCATOR.paramList.add(new CParameter(CType.SEGMENT_ALLOCATOR, "allocator"));
        SUPER_CALL_ALLOCATOR.code.append("super(allocator);");

        SUPER_CALL_ASSIGN.accessSpecifier.pub = true;
        SUPER_CALL_ASSIGN.paramList.add(new CParameter(CType.MEMORY_SEGMENT, "segment"));
        SUPER_CALL_ASSIGN.code.append("super(segment);");

    }
    public final CClass implementation = new CClass();
    private final GroupLayout layout;
    public final Class<?> parent;
    private boolean valGetSetImplemented = false;

    public Struct(GroupLayout layout, String pkg, Class<?> parent) {
        this.layout = layout;
        this.parent = parent;
        implementation.pkg = pkg;
        implementation.importImplicitly(new CType(parent));
        implementation.accessSpecifier.pub = true;
        implementation.name = parent.getSimpleName() + "_J";
        implementation.addField(SEGMENT_FIELD);
        implementation.addConstructor(genSetterConstructor());
        implementation.addConstructor(genAllocatorConstructor(parent));
    }

    public Struct(String pkg, Class<?> parent, Class<?> baseImpl) {
        this.layout = null;
        this.parent = parent;
        var baseWrapper = new CType(pkg + "." + baseImpl.getSimpleName() + "_J", baseImpl.getSimpleName() + "_J", false);
        implementation.pkg = pkg;
        implementation.accessSpecifier.pub = true;
        implementation.name = parent.getSimpleName() + "_J";
        implementation.superclass = baseWrapper;
        implementation.addConstructor(SUPER_CALL_ASSIGN);
        implementation.addConstructor(SUPER_CALL_ALLOCATOR);
    }

    public boolean isBaseImplementation() {
        return this.layout != null;
    }

    private static CConstructor genSetterConstructor() {
        var constructor = new CConstructor();
        constructor.accessSpecifier.pub = true;
        constructor.paramList.add(new CParameter(CType.MEMORY_SEGMENT, "segment"));
        constructor.code.append("this.segment = segment;");
        return constructor;
    }

    private static CConstructor genAllocatorConstructor(Class<?> parent) {
        var constructor = new CConstructor();
        constructor.accessSpecifier.pub = true;
        constructor.paramList.add(new CParameter(CType.SEGMENT_ALLOCATOR, "allocator"));
        constructor.code.append("this(").append(parent.getSimpleName()).append(".allocate(allocator));");
        return constructor;
    }

    public boolean layoutEqualTo(MemoryLayout layout) {
        return layout.withName("").equals(this.layout.withName(""));
    }

    public void implementValueLayoutGetterSetters() {
        if (!isBaseImplementation()) return;
        if (valGetSetImplemented) return;
        valGetSetImplemented = true;
        layout.memberLayouts()
                .stream()
                .filter(ValueLayout.class::isInstance)
                .map(ValueLayout.class::cast)
                .flatMap((value) -> {
                    @SuppressWarnings("OptionalGetWithoutIsPresent") //A name is always present, no need to check
                    var name = value.name().get();
                    var getter = new CMethod();
                    var setter = new CMethod();
                    getter.accessSpecifier.pub = setter.accessSpecifier.pub = true;
                    getter.name = setter.name = name;
                    var oGetter = Arrays.stream(parent.getMethods()).filter((method) -> method.getParameterCount() == 1 && method.getName().equals(name + "$get")).findFirst();
                    if (oGetter.isEmpty()) {
                        System.err.println("Could not generate getter/setter for struct field " + name);
                        return Stream.empty();
                    }
                    var type = new CType(oGetter.get().getReturnType());
                    setter.paramList.add(new CParameter(getter.returnType = type, "value"));
                    getter.code.append("return ").append(parent.getSimpleName()).append('.').append(name).append("$get(segment);");
                    setter.code.append(parent.getSimpleName()).append('.').append(name).append("$set(segment, value);");
                    return Stream.of(getter, setter);
                })
                .forEach(implementation::addMethod);
    }

    public void implementGroupLayoutGetters(List<Struct> allStructs) {
        if (!isBaseImplementation()) return;
        var structMapping = new HashMap<GroupLayout, Struct>();
        allStructs.stream()
                .filter(Struct::isBaseImplementation)
                .filter((other) -> !(other.parent.isAssignableFrom(parent) || parent.isAssignableFrom(other.parent)))
                .forEach((other) -> layout.memberLayouts().stream()
                        .filter(other::layoutEqualTo)
                        .filter(GroupLayout.class::isInstance)
                        .map(GroupLayout.class::cast)
                        .forEach((subLayout) -> {
                            if (subLayout.name().orElse("$anon$").startsWith("$anon$")) return;
                            if (other.layoutEqualTo(subLayout)) {
                                if (structMapping.containsKey(subLayout)) {
                                    var that = structMapping.get(subLayout);
                                    boolean thisTag = other.implementation.name.startsWith("tag");
                                    boolean thatTag = that.implementation.name.startsWith("tag");
                                    if (thisTag == thatTag)
                                        throw new IllegalStateException("Struct collision! " + structMapping.get(subLayout).parent + ", " + other.parent);
                                    else if (!thisTag) {
                                        structMapping.put(subLayout, other);
                                    }
                                }
                                structMapping.put(subLayout, other);
                            }
                        }));
        structMapping.forEach(this::setupGroupLayoutGetter);
    }

    private void setupGroupLayoutGetter(GroupLayout layout, Struct struct) {
        @SuppressWarnings("OptionalGetWithoutIsPresent") //A name is always present, no need to check
        var name = layout.name().get();
        var cType = struct.implementation.asCType();

        var field = new CField();
        implementation.constructors.get(0).code.append("\n").append(name).append(" = new ").append(cType.simpleName()).append("(").append(parent.getSimpleName()).append(".").append(name).append("$slice(segment));");
        field.accessSpecifier.pub = field.accessSpecifier.fin = true;
        field.name = name;
        field.type = cType;
        implementation.addField(field);
    }

    public void implementSequenceLayoutGetterSetter(List<Struct> allStructs) {

    }
}

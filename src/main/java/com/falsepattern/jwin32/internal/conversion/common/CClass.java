package com.falsepattern.jwin32.internal.conversion.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CClass {
    public String pkg;
    public final AccessSpecifier accessSpecifier = new AccessSpecifier();
    public String name;
    public CType superclass;
    public final Set<CType> imports = new HashSet<>();
    public final List<CField> fields = new ArrayList<>();
    public final List<CConstructor> constructors = new ArrayList<>();
    public final List<CMethod> methods = new ArrayList<>();

    public void importImplicitly(CType type) {
        imports.add(type);
    }

    public void addField(CField field) {
        imports.add(field.type);
        fields.add(field);
    }

    public void addConstructor(CConstructor constructor) {
        imports.addAll(constructor.getTypes());
        constructors.add(constructor);
    }

    public void addMethod(CMethod method) {
        imports.addAll(method.getTypes());
        methods.add(method);
    }

    public void superConstructors(CClass other) {

        constructors.forEach((constructor) -> {
            if (!constructor.accessSpecifier.pub) return;
            var newConstructor = new CConstructor();
            newConstructor.accessSpecifier.pub = true;
            newConstructor.paramList.parameters.addAll(constructor.paramList.parameters);
            newConstructor.code.append("super(").append(constructor.paramList.asFunctionCallParams()).append(");");
            other.addConstructor(newConstructor);
        });
    }

    public CType asCType() {
        return new CType(pkg + "." + name, name, false);
    }

    @Override
    public String toString() {
        return """
               package %s;
               
               %s
               %s
               %sclass %s%s {
               %s
               %s
               %s
               }
               """.formatted(
                       pkg,
                imports.stream().filter(Predicate.not(CType::primitive)).filter(imp -> !imp.name().equals(pkg + "." + imp.simpleName())).map(CType::asImport).sorted().collect(Collectors.joining()),
                superclass != null ? superclass.name().equals(pkg + "." + superclass.simpleName()) ? "" : superclass.name() : "",
                accessSpecifier,
                name,
                superclass != null ? " extends " + superclass.simpleName() : "",
                fields.stream().map(CField::toString).collect(Collectors.joining("\n")).indent(4),
                constructors.stream().map(constructor -> constructor.toString(name)).collect(Collectors.joining("\n")).indent(4),
                methods.stream().map(CMethod::toString).collect(Collectors.joining("\n")).indent(4));
    }
}

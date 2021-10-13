package com.falsepattern.jwin32.conversion.common;

public record CType(String name, String simpleName, boolean primitive) {
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
}

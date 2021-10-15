package com.falsepattern.jwin32.conversion.common;

public class CField {
    public final AccessSpecifier accessSpecifier = new AccessSpecifier();
    public CType type;
    public String name;
    public final StringBuilder initializer = new StringBuilder();
    @Override
    public String toString() {
        return "%s%s %s%s;".formatted(accessSpecifier.toString(), type.simpleName(), name, initializer.length() > 0 ? " = " + initializer : "");
    }

    public CType type(){return type;}
}

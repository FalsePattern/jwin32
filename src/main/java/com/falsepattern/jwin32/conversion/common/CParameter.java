package com.falsepattern.jwin32.conversion.common;

public record CParameter(CType type, String name) {
    @Override
    public String toString() {
        return type.simpleName() + " " + name;
    }

}

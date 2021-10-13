package com.falsepattern.jwin32.conversion.common;

import java.util.Set;

public class CConstructor implements TypeCarrier {
    public final AccessSpecifier accessSpecifier = new AccessSpecifier();
    public final CParamList paramList = new CParamList();
    public final StringBuilder code = new StringBuilder();

    public String toString(String className) {
        return "%s%s(%s) {\n%s}\n".formatted(
                accessSpecifier.toString(),
                className,
                paramList.toString(),
                code.toString().indent(4));
    }

    @Override
    public Set<CType> getTypes() {
        return paramList.getTypes();
    }
}
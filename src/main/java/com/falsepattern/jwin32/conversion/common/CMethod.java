package com.falsepattern.jwin32.conversion.common;

import java.util.HashSet;
import java.util.Set;

public class CMethod implements TypeCarrier {
    public final AccessSpecifier accessSpecifier = new AccessSpecifier();
    public CType returnType = CType.VOID;
    public String name;
    public CParamList paramList = new CParamList();
    public final StringBuilder code = new StringBuilder();

    @Override
    public String toString() {
        return "%s%s %s(%s){\n%s}".formatted(
                accessSpecifier.toString(),
                returnType.simpleName(),
                name,
                paramList.toString(),
                code.toString().indent(4));
    }

    @Override
    public Set<CType> getTypes() {
        var result = new HashSet<CType>();
        result.add(returnType);
        result.addAll(paramList.getTypes());
        return result;
    }
}

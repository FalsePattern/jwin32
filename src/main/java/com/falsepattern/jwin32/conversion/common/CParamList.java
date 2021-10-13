package com.falsepattern.jwin32.conversion.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CParamList implements TypeCarrier {
    public final List<CParameter> parameters = new ArrayList<>();

    public void add(CParameter parameter) {
        parameters.add(parameter);
    }

    @Override
    public Set<CType> getTypes() {
        return parameters.stream().map(CParameter::type).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String toString() {
        return parameters.stream().map(CParameter::toString).collect(Collectors.joining(", "));
    }

    public String asFunctionCallParams() {
        return parameters.stream().map(CParameter::name).collect(Collectors.joining(", "));
    }
}

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
package com.falsepattern.jwin32.internal.conversion.common;

import java.util.HashSet;
import java.util.Set;

public class CMethod implements TypeCarrier {
    public final AccessSpecifier accessSpecifier = new AccessSpecifier();
    public CType returnType = CType.VOID;
    public String name;
    public final CParamList paramList = new CParamList();
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

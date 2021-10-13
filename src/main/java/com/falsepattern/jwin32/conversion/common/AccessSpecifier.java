package com.falsepattern.jwin32.conversion.common;

public class AccessSpecifier {
    public boolean pub = false;
    public boolean stat = false;
    public boolean fin = false;
    @Override
    public String toString() {
        return
                "%s%s%s".formatted(pub ? "public " : "private ", stat ? "static " : "", fin ? "final " : "");
    }
}

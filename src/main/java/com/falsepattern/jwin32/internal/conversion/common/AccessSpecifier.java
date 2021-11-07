package com.falsepattern.jwin32.internal.conversion.common;

public class AccessSpecifier {
    public enum Visibility {
        PUBLIC, PACKAGE, PRIVATE
    }
    public Visibility vis = Visibility.PRIVATE;
    public boolean stat = false;
    public boolean fin = false;
    @Override
    public String toString() {
        return
                "%s%s%s".formatted(switch (vis) {
                    case PUBLIC -> "public ";
                    case PACKAGE -> "";
                    case PRIVATE -> "private ";
                }, stat ? "static " : "", fin ? "final " : "");
    }
}

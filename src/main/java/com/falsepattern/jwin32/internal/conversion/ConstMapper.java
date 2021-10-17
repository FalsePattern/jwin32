package com.falsepattern.jwin32.internal.conversion;

import com.falsepattern.jwin32.internal.conversion.common.CClass;
import com.falsepattern.jwin32.internal.conversion.common.CField;
import com.falsepattern.jwin32.internal.conversion.common.CType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ConstMapper {
    private static final Pattern DEFINED_BYTE = Pattern.compile(" {4}public static byte (\\w+)\\(\\) \\{\\n {8}return (\\(byte\\)-?\\d+L);\\n {4}}\n");
    private static final Pattern DEFINED_SHORT = Pattern.compile(" {4}public static short (\\w+)\\(\\) \\{\\n {8}return (\\(short\\)-?\\d+L);\\n {4}}\n");
    private static final Pattern DEFINED_INT = Pattern.compile(" {4}public static int (\\w+)\\(\\) \\{\\n {8}return (\\(int\\)-?\\d+L);\\n {4}}\n");
    private static final Pattern DEFINED_LONG = Pattern.compile(" {4}public static long (\\w+)\\(\\) \\{\\n {8}return (-?\\d+L);\\n {4}}\n");
    private static final Pattern DEFINED_FLOAT = Pattern.compile(" {4}public static float (\\w+)\\(\\) \\{\\n {8}return (-?\\d+\\.?\\d*E?\\d*f);\\n {4}}\n");
    private static final Pattern DEFINED_DOUBLE = Pattern.compile(" {4}public static double (\\w+)\\(\\) \\{\\n {8}return (-?\\d+\\.?\\d*E?\\d*d);\\n {4}}\n");

    private static final Map<CType, Pattern> patterns = new HashMap<>();
    static {
        patterns.put(CType.BYTE, DEFINED_BYTE);
        patterns.put(CType.SHORT, DEFINED_SHORT);
        patterns.put(CType.INT, DEFINED_INT);
        patterns.put(CType.LONG, DEFINED_LONG);
        patterns.put(CType.FLOAT, DEFINED_FLOAT);
        patterns.put(CType.DOUBLE, DEFINED_DOUBLE);
    }
    public static CClass extractAllConstants(String pkg, String className, List<File> files) {
        var clazz = new CClass();
        clazz.accessSpecifier.pub = true;
        clazz.pkg = pkg;
        clazz.name = className;
        files.stream()
                .filter((file) -> file.getName().matches("Win32(?:_\\d+)?.java"))
                .flatMap((file) -> {
                    final String[] contents = new String[1];
                    try {
                        contents[0] = Files.readString(file.toPath());
                    } catch (IOException e) {
                        System.err.println("Failed to read " + file);
                        return Stream.empty();
                    }
                    var fields = new LinkedList<CField>();
                    patterns.forEach((type, pattern) -> {
                        var matcher = pattern.matcher(contents[0]);
                        var fileRemnant = new StringBuilder();
                        int lastEnd = 0;
                        while (matcher.find()) {
                            var field = new CField();
                            field.accessSpecifier.pub = field.accessSpecifier.stat = field.accessSpecifier.fin = true;
                            field.type = type;
                            field.name = matcher.group(1);
                            field.initializer.append(matcher.group(2));
                            fields.add(field);
                            fileRemnant.append(contents[0], lastEnd, matcher.start());
                            lastEnd = matcher.end();
                        }
                        if (lastEnd != 0) {
                            fileRemnant.append(contents[0], lastEnd, contents[0].length());
                            contents[0] = fileRemnant.toString();
                        }
                    });
                    try {
                        Files.writeString(file.toPath(), contents[0]);
                    } catch (IOException e) {
                        System.err.println("Failed to write back cleaned file " + file);
                    }
                    return fields.stream();
                })
                .sorted(Comparator.comparing((field) -> field.name))
                .forEach(clazz::addField);
        return clazz;

    }

    public static CClass mapDefines(String pkg, String className, Predicate<CField> filter, CClass definesClass) {
        var defType = definesClass.asCType();
        var clazz = new CClass();
        clazz.accessSpecifier.pub = clazz.accessSpecifier.fin = true;
        clazz.name = className;
        clazz.pkg = pkg;
        clazz.importImplicitly(defType);
        definesClass.fields.stream()
                .filter(filter)
                .map((field) -> {
                    var mappedField = new CField();
                    mappedField.accessSpecifier.pub = mappedField.accessSpecifier.stat = mappedField.accessSpecifier.fin = true;
                    mappedField.name = field.name;
                    mappedField.type = field.type;
                    mappedField.initializer.append(defType.simpleName()).append(".").append(field.name);
                    return mappedField;
                })
                .sorted(Comparator.comparing((field) -> field.name))
                .forEach(clazz::addField);
        return clazz;
    }
}

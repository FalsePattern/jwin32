package com.falsepattern.jwin32.conversion;

import com.falsepattern.jwin32.conversion.common.CClass;
import com.falsepattern.jwin32.conversion.common.CField;
import com.falsepattern.jwin32.conversion.common.CType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ConstMapper {
    private static final Pattern DEFINED_INT = Pattern.compile("public static int (\\w+)\\(\\) \\{\\n {8}return (\\(int\\)\\d+L);\\n {4}}\n");
    public static CClass extractAllConstants(String pkg, String className, List<File> files) {
        var clazz = new CClass();
        clazz.accessSpecifier.pub = true;
        clazz.pkg = pkg;
        clazz.name = className;
        files.stream()
                .filter((file) -> file.getName().matches("Win32(?:_\\d+)?.java"))
                .map(File::toPath)
                .map((path) -> {
                    try {
                        return Files.readString(path);
                    } catch (IOException e) {
                        System.err.println("Failed to read " + path);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap((fileContents) -> {
                    var matcher = DEFINED_INT.matcher(fileContents);
                    var fields = new LinkedList<CField>();
                    while (matcher.find()) {
                        var field = new CField();
                        field.accessSpecifier.pub = field.accessSpecifier.stat = field.accessSpecifier.fin = true;
                        field.type = CType.INT;
                        field.name = matcher.group(1);
                        field.initializer.append(matcher.group(2));
                        fields.add(field);
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

package com.falsepattern.jwin32.internal.conversion;

import com.falsepattern.jwin32.internal.conversion.common.AccessSpecifier;
import com.falsepattern.jwin32.internal.conversion.common.CClass;
import com.falsepattern.jwin32.internal.conversion.common.CField;
import com.falsepattern.jwin32.internal.conversion.common.CType;
import com.falsepattern.jwin32.memory.MemoryUtil;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import win32.pure.Win32;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final Pattern DEFINED_STRING = Pattern.compile(" {4}public static MemorySegment (\\w+)\\(\\) \\{\\n {8}return constants\\$\\d+\\..*?;\\n {4}}\n");
    private static final Pattern DEFINED_POINTER = Pattern.compile(" {4}public static MemoryAddress (\\w+)\\(\\) \\{\\n {8}return constants\\$\\d+\\..*?;\\n {4}}\n");

    private static final Map<CType, Pattern> patterns = new HashMap<>();
    static {
        patterns.put(CType.BYTE, DEFINED_BYTE);
        patterns.put(CType.SHORT, DEFINED_SHORT);
        patterns.put(CType.INT, DEFINED_INT);
        patterns.put(CType.LONG, DEFINED_LONG);
        patterns.put(CType.FLOAT, DEFINED_FLOAT);
        patterns.put(CType.DOUBLE, DEFINED_DOUBLE);
        patterns.put(CType.MEMORY_ADDRESS, DEFINED_POINTER);
        patterns.put(CType.MEMORY_SEGMENT, DEFINED_STRING);
    }
    public static CClass[] extractAllConstants(String pkg, List<File> files) {
        var classes = new ArrayList<CClass>();
        var ref = new Object() {
            CClass clazz = new CClass();
        };
        ref.clazz.accessSpecifier.vis = AccessSpecifier.Visibility.PACKAGE;
        ref.clazz.pkg = pkg;
        ref.clazz.name = "Constants$0";
        AtomicInteger id = new AtomicInteger();
        AtomicInteger constantCount = new AtomicInteger();
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
                            field.accessSpecifier.vis = AccessSpecifier.Visibility.PUBLIC;
                            field.accessSpecifier.stat = field.accessSpecifier.fin = true;
                            field.name = matcher.group(1);
                            if (type.equals(CType.MEMORY_ADDRESS) || type.equals(CType.MEMORY_SEGMENT)) {
                                field.type = type.equals(CType.MEMORY_ADDRESS) ? CType.LONG : CType.STRING;
                                try {
                                    var method = Win32.class.getMethod(field.name);
                                    method.setAccessible(true);
                                    var value = method.invoke(null);
                                    if (type.equals(CType.MEMORY_ADDRESS)) {
                                        var offset = ((MemoryAddress)value).segmentOffset(MemorySegment.globalNativeSegment());
                                        field.initializer.append(offset).append('L');
                                    } else {
                                        var str = CLinker.toJavaString((MemorySegment) value);
                                        field.initializer.append('"').append(escape(str)).append('"');
                                    }
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                field.type = type;
                                field.initializer.append(matcher.group(2));
                            }
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
                .forEach((field) -> {
                    ref.clazz.addField(field);
                    if (constantCount.incrementAndGet() >= 4096) {
                        classes.add(ref.clazz);
                        var newClass = new CClass();
                        newClass.name = "Constants$" + id.incrementAndGet();
                        newClass.accessSpecifier.vis = AccessSpecifier.Visibility.PACKAGE;
                        newClass.pkg = pkg;
                        newClass.superclass = ref.clazz.asCType();
                        ref.clazz = newClass;
                        constantCount.set(0);
                    }
                });
        ref.clazz.accessSpecifier.vis = AccessSpecifier.Visibility.PUBLIC;
        ref.clazz.name = "Constants";
        classes.add(ref.clazz);
        return classes.toArray(CClass[]::new);
    }
    private static String escape(String s){
        return s.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f")
                .replace("\"", "\\\"");
    }
}

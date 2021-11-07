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
package com.falsepattern.jwin32.internal.conversion;

import com.falsepattern.jwin32.internal.conversion.common.CClass;
import jdk.incubator.foreign.GroupLayout;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class Translator {

    private static final String[] BANNED_CLASSES;

    static {
        try {
            BANNED_CLASSES = Files.readAllLines(Path.of("./banned_structs")).toArray(String[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        var files = Arrays.asList(Objects.requireNonNull(new File("./src/main/java/win32/pure").listFiles()));
        var comObjects = new ArrayList<CClass>();
        var structs = new HashMap<Class<?>, Struct>();
        //filter files
        var fNames = files.stream()
                .map(File::getName)
                .map((str) -> str.substring(0, str.lastIndexOf('.')))
                .filter((fName) -> !fName.startsWith("constants$"))
                .filter((fName) -> Arrays.stream(BANNED_CLASSES).noneMatch(Predicate.isEqual(fName)))
                .toList();
        //First pass
        for (var fName: fNames) {
            try {
                if (fName.endsWith("Vtbl")) {
                    //Vtbls implicitly have a base class too
                    var comObj = COMGenerator.generateCOM("win32.mapped.com",
                            Class.forName("win32.pure." + fName.substring(0, fName.lastIndexOf("Vtbl")), false, Translator.class.getClassLoader()),
                            Class.forName("win32.pure." + fName, false, Translator.class.getClassLoader())
                    );
                    comObjects.add(comObj);
                    System.out.println("Generated wrapper for COM object " + fName.substring(0, fName.lastIndexOf("Vtbl")));
                } else {
                    if (fNames.stream().anyMatch((f2Name) -> f2Name.equals(fName + "Vtbl"))) continue;
                    var baseClass = Class.forName("win32.pure." + fName, false, Translator.class.getClassLoader());
                    if (structs.containsKey(baseClass)) continue;
                    Struct struct;
                    Method method;
                    try {
                        method = baseClass.getDeclaredMethod("$LAYOUT");
                    } catch (NoSuchMethodException e) {
                        continue;
                    }
                    try {
                        var layout = (GroupLayout) method.invoke(null);
                        Optional<Struct> that = structs.values().stream().filter(Struct::isBaseImplementation).filter((other) -> other.layoutEqualTo(layout)).findFirst();
                        if (that.isEmpty()) {
                            struct = new Struct((GroupLayout) method.invoke(null), "win32.mapped.struct", baseClass);
                            System.out.println("Generated wrapper for struct " + baseClass.getSimpleName());
                        } else {
                            struct = new Struct("win32.mapped.struct", baseClass, that.get().parent);
                            System.out.println("Mapped logically identical struct " + baseClass.getSimpleName() + " -> " + that.get().parent.getSimpleName());
                        }
                    } catch (ExceptionInInitializerError e) {
                        System.err.println("Broken struct: \"" + baseClass.getSimpleName() + "\"");
                        continue;
                    }
                    struct.implementValueLayoutGetterSetters();
                    structs.put(baseClass, struct);
                }
            } catch (ClassNotFoundException e) {
                throw new Error(e);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        //second pass
        pass:
        for (var fName: fNames) {
            try {
                if (!fName.endsWith("Vtbl") || fNames.stream().noneMatch((f2Name) -> f2Name.equals(fName + "Vtbl"))) {
                    var baseClass = Class.forName("win32.pure." + fName, false, Translator.class.getClassLoader());
                    if (structs.containsKey(baseClass)) continue;
                    if (!Object.class.equals(baseClass.getSuperclass())) {
                        Class<?> superClass;
                        Class<?> prevSuperClass = baseClass;
                        while ((superClass = prevSuperClass.getSuperclass()) != null && !Object.class.equals(superClass)) {
                            if (Arrays.stream(BANNED_CLASSES).anyMatch(Predicate.isEqual(superClass.getSimpleName())))
                                continue pass;
                            prevSuperClass = superClass;
                        }
                        if (structs.containsKey(prevSuperClass)) {
                            structs.put(baseClass, new Struct("win32.mapped.struct", baseClass, prevSuperClass));
                            System.out.println("Mapped struct " + baseClass.getSimpleName() + " -> " + prevSuperClass.getSimpleName());
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new Error(e);
            }
        }

        System.out.println("Generating substruct getters");
        var structList = structs.values().stream().toList();
        structList.stream().parallel().filter(Struct::isBaseImplementation).forEach((struct) -> {
            struct.implementGroupLayoutGetters(structList);
            System.out.println("Generated substruct getters for " + struct.parent.getSimpleName());
        });

        System.out.println("Generated wrapper classes for " + structs.size() + " structs and " + comObjects.size() + " COM objects!");

        comObjects.stream().parallel().forEach((comObj) -> {
            try {
                Files.writeString(Path.of("./src/main/java/win32/mapped/com/" + comObj.name + ".java"), comObj.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        structs.values().stream().parallel().forEach((struct) -> {
            try {
                Files.writeString(Path.of("./src/main/java/win32/mapped/struct/" + struct.implementation.name + ".java"), struct.implementation.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        {
            System.out.println("Processing constants...");
            var constants = ConstMapper.extractAllConstants("win32.mapped.constants", files);
            for (var c: constants) {
                Files.writeString(Path.of("./src/main/java/win32/mapped/constants/" + c.name + ".java"), c.toString());
            }
        }
        System.out.println("Transformations complete!");
        System.out.println("Generating module info...");
        Files.writeString(Path.of("./src/main/java/module-info.java"), """
                module jwin32_ {
                    requires transitive jdk.incubator.foreign;
                    exports com.falsepattern.jwin32.memory;
                """ + (comObjects.size() > 0 ? "    exports win32.mapped.com;" : "") + """
                    exports win32.mapped.constants;
                    exports win32.mapped.struct;
                    exports win32.pure;
                }""");
        System.out.println("Module info generated!");
    }
}

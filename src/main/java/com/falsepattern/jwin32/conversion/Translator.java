package com.falsepattern.jwin32.conversion;

import com.falsepattern.jwin32.conversion.common.CClass;
import com.falsepattern.jwin32.conversion.common.CConstructor;
import com.falsepattern.jwin32.conversion.common.CField;
import com.falsepattern.jwin32.conversion.common.CType;
import win32.pure.Win32;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Translator {
    public static void main(String[] args) throws IOException {

        var files = Arrays.asList(Objects.requireNonNull(new File("./src/main/java/win32/pure").listFiles()));
        var comObjects = new ArrayList<CClass>();
        var structs = new HashMap<Class<?>, CClass>();
        boolean doCom = true;
        for (int i = 0; i < 2; i++) {
            boolean finalDoCom = doCom;
            files.stream().parallel().forEach((file) -> {
                try {
                    var fName = file.getName().substring(0, file.getName().lastIndexOf('.'));
                    if (fName.endsWith("Vtbl")) {
                        if (!finalDoCom) return;
                        //Vtbls implicitly have a base class too
                        var comObj = COMGenerator.generateCOM("win32.mapped.com",
                                Class.forName("win32.pure." + fName.substring(0, fName.lastIndexOf("Vtbl")), false, Translator.class.getClassLoader()),
                                Class.forName("win32.pure." + fName, false, Translator.class.getClassLoader())
                        );
                        synchronized (comObjects) {
                            comObjects.add(comObj);
                        }
                    } else {
                        if (!fName.startsWith("constants$") && files.stream().noneMatch((file2) -> {
                            var f2Name = file2.getName().substring(0, file2.getName().lastIndexOf('.'));
                            return f2Name.equals(fName + "Vtbl");
                        })) {
                            //$struct$LAYOUT
                            var baseClass = Class.forName("win32.pure." + fName, false, Translator.class.getClassLoader());
                            if (structs.containsKey(baseClass)) return;
                            var superClass = baseClass.getSuperclass();
                            CClass struct = null;
                            if (!Object.class.equals(superClass)) {
                                if (structs.containsKey(superClass)) {
                                    var sup = structs.get(superClass);
                                    struct = new CClass();
                                    struct.pkg = "win32.mapped.struct";
                                    struct.name = baseClass.getSimpleName() + "_J";
                                    struct.superclass = structs.get(superClass).asCType();
                                    struct.accessSpecifier.pub = true;
                                    sup.superConstructors(struct);
                                }
                            }
                            if (struct == null && Files.readString(file.toPath()).contains("$struct$LAYOUT"))
                                struct = StructGenerator.generateStruct("win32.mapped.struct", baseClass);
                            if (struct != null) {
                                synchronized (structs) {
                                    structs.put(baseClass, struct);
                                }
                            }
                        }
                    }
                } catch (ClassNotFoundException | IOException e) {
                    throw new Error(e);
                }
            });
            doCom = false;
        }

        comObjects.stream().parallel().forEach((comObj) -> {
            try {
                Files.writeString(Path.of("./src/main/java/win32/mapped/com/" + comObj.name + ".java"), comObj.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        structs.values().stream().parallel().forEach((struct) -> {
            try {
                Files.writeString(Path.of("./src/main/java/win32/mapped/struct/" + struct.name + ".java"), struct.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //WM_values
        var fields = Arrays.stream(Win32.class.getMethods())
                .parallel()
                .filter((method) -> method.getName().startsWith("WM_") && method.getReturnType().equals(int.class))
                .map((method) -> {
                    method.setAccessible(true);
                    var field = new CField();
                    field.accessSpecifier.pub = field.accessSpecifier.stat = field.accessSpecifier.fin = true;
                    field.name = method.getName();
                    field.type = new CType(int.class);
                    try {
                        field.initializer.append("(int)").append(method.invoke(null));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new Error(e);
                    }
                    return field;
                })
                .sorted(Comparator.comparing((field) -> field.name))
                .toList();

        var clazz = new CClass();
        clazz.accessSpecifier.pub = clazz.accessSpecifier.fin = true;
        clazz.name = "WindowMessages";
        clazz.pkg = "win32.mapped";
        fields.forEach(clazz::addField);
        clazz.addConstructor(new CConstructor());
        Files.writeString(Path.of("./src/main/java/win32/mapped/WindowMessages.java"), clazz.toString());

    }
}

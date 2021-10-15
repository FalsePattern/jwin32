package com.falsepattern.jwin32.conversion;

import com.falsepattern.jwin32.conversion.common.CClass;
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

    @SuppressWarnings("SpellCheckingInspection")
    private static final String[] BANNED_CLASSES = new String[]{
            "_MMIOINFO",
            "DRVCONFIGINFOEX",
            "IMAGE_AUX_SYMBOL_TOKEN_DEF",
            "tagDRVCONFIGINFO",
            "midihdr_tag",
            "tagMCI_ANIM_OPEN_PARMSA",
            "tagMCI_ANIM_OPEN_PARMSW",
            "tagMCI_ANIM_WINDOW_PARMSA",
            "tagMCI_ANIM_WINDOW_PARMSW",
            "tagMCI_BREAK_PARMS",
            "tagMCI_OPEN_PARMSA",
            "tagMCI_OPEN_PARMSW",
            "tagMCI_OVLY_OPEN_PARMSA",
            "tagMCI_OVLY_OPEN_PARMSW",
            "tagMCI_OVLY_WINDOW_PARMSA",
            "tagMCI_OVLY_WINDOW_PARMSW",
            "tagMCI_WAVE_OPEN_PARMSA",
            "tagMCI_WAVE_OPEN_PARMSW",
            "tagMETAHEADER",
            "tagMIXERLINEA",
            "tagMIXERLINECONTROLSA",
            "tagMIXERLINECONTROLSW",
            "tagMIXERLINEW",
            "tagBITMAPFILEHEADER",
            "tMIXERCONTROLDETAILS"
    };
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


        System.out.println("Discovering integer constants...");
        var constants = ConstMapper.extractAllConstants("win32.mapped", "Constants", files);
        System.out.println("Discovered " + constants.fields.size() + " constants!");
        Files.writeString(Path.of("./src/main/java/win32/mapped/Constants.java"), constants.toString());

    }
}

module jwin32_ {
    requires transitive jdk.incubator.foreign;
    exports com.falsepattern.jwin32.memory;
    exports win32.mapped.com;
    exports win32.mapped.constants;
    exports win32.mapped.struct;
    exports win32.pure;
}
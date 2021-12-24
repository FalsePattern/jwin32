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

package com.falsepattern.jwin32.internal.guid;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Finds all GUIDs which were pulled in by jextract.
 */
public class GUIDHunter implements Consumer<File> {
    private final Map<String, String> guidMap = new HashMap<>();

    //Gigantic regex pattern to match the third party code's declarations
    private final Pattern guidRegex = Pattern.compile("""
\\s*\\[NativeTypeName\\("const GUID"\\)]\
\\s*public static ref readonly Guid (\\w+)\
\\s*\\{\
\\s*\\[MethodImpl\\(MethodImplOptions.AggressiveInlining\\)]\
\\s*get\
\\s*\\{\
\\s*ReadOnlySpan<byte> data = new byte\\[] \\{\
\\s*(0x\\w\\w), (0x\\w\\w), (0x\\w\\w), (0x\\w\\w),\
\\s*(0x\\w\\w), (0x\\w\\w),\
\\s*(0x\\w\\w), (0x\\w\\w),\
\\s*(0x\\w\\w),\
\\s*(0x\\w\\w),\
\\s*(0x\\w\\w),\
\\s*(0x\\w\\w),\
\\s*(0x\\w\\w),\
\\s*(0x\\w\\w),\
\\s*(0x\\w\\w),\
\\s*(0x\\w\\w)\
"""); //Don't care about the rest

    private final Pattern jextractGUIDRegex = Pattern.compile("""
static final MemorySegment \\w+\\$SEGMENT = RuntimeHelper\\.lookupGlobalVariable\\(Win32\\.LIBRARIES, "(\\w+)", constants\\$\\d+.\\w+\\$LAYOUT\\);
""");

    public GUIDHunter(File rootDirectory) {
        System.out.println("Initializing GUIDHunter...");
        var iter = new FileTreeIterator(this);
        iter.iterate(rootDirectory);
        System.out.println("GUIDHunter discovered " + guidMap.size() + " GUIDs!");
    }

    @Override
    public void accept(File file) {
        String contents;
        try {
            contents = Files.readString(file.toPath());
        } catch (IOException e) {
            System.err.println("Failed to read " + file.getAbsolutePath());
            e.printStackTrace();
            return;
        }
        var matcher = guidRegex.matcher(contents);
        boolean found = false;
        while (matcher.find()) {
            found = true;
            guidMap.put(matcher.group(1),"new byte[] {" + IntStream.range(2, 18).mapToObj(matcher::group).map((str) -> "(byte)" + str).collect(Collectors.joining(",")) + "}");
        }
        if (!found) {
            System.err.println("Couldn't find any GUID definitions in file: " + file.getAbsolutePath());
        }
    }

    public String injectIntoMappings(List<File> files) {
        var log = new StringBuilder();
        files.parallelStream().forEach((file) -> {
            try {
                Files.writeString(file.toPath(),
                        jextractGUIDRegex.matcher(Files.readString(file.toPath()))
                                .replaceAll((match) -> {
                                    var guidName = match.group(1);
                                    if (guidMap.containsKey(guidName)) {
                                        return ("static final MemorySegment " + guidName + "$SEGMENT = MemorySegment.allocateNative(16, MemorySegment.globalNativeSegment().scope());\n" +
                                                "static {" + guidName + "$SEGMENT.copyFrom(MemorySegment.ofArray(" + guidMap.get(guidName) + "));}\n").replace("$", "\\$");
                                    } else if (guidName.contains("IID")) {
                                        synchronized (log) {
                                            log.append("GUID mapping not found: ").append(guidName).append('\n');
                                        }
                                    }
                                    return match.group().replace("$", "\\$");
                }));
            } catch (IOException e) {
                System.err.println("Failed to process GUID mappings for file: " + file.getAbsolutePath());
                e.printStackTrace();
            }
        });
        return log.toString();
    }
}

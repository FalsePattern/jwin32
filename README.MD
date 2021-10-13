# Win32Natives
A Project Panama-based mapping + wrapper generator for win32 headers.

## Table of Contents
0. [Disclaimer](#0-disclaimer)
1. [Dependencies](#1-dependencies)
2. [Generating panama mappings](#2-generating-panama-mappings)
3. [Generating wrappers](#3-generating-wrappers)
4. [Installing to local maven repository](#4-installing-to-local-maven-repository)
5. [Running code with panama](#5-running-code-with-panama)
6. [Linking libraries](#6-linking-libraries)
7. [What next?](#7-what-next)
8. [Inspiration](#8-inspiration)

## 0. Disclaimer
This project is not endorsed by nor affiliated with either Microsoft, Oracle, nor the OpenJDK project.

MSVC, Windows.h, d3d11.h and the Windows SDK are owned by Microsoft, and due to licensing issues, are not included as a part of this source code.

The project source code does not include generated and mapped files for two reasons:
1. There's a lot of files (more than 10k after every class is generated)
2. There might be licensing issues with some Windows.h struct names (see the [disclaimer](#0-disclaimer))

## 1. Dependencies
1. ### Project Panama
   1. Download and extract JDK panama from this URL: https://jdk.java.net/panama
        
       (The latest version at the time of writing is `17-panama+3-167`)
   2. Next, you have to replace your currently installed java with Panama in the PATH and JAVA_HOME system variables:
     
       Inside PATH, replace your current java path with, or, if you don't have one, add: `<Directory where you extracted panama>\jdk-17-panama\bin`
     
       Set JAVA_HOME to: `<Directory where you extracted panama>\jdk-17-panama`
   3. To verify that panama has been added to PATH properly, open a command prompt, and type `java - version`. 
   The first output line should start with `openjdk version "17-panama"`.
   4. Also enter `jextract -version`into the same command prompt. If the output starts with
      `WARNING: Using incubator modules: jdk.incubator.foreign, jdk.incubator.jextract`, panama is correctly
      installed.
3. ### Windows SDK
   After you've installed project panama, you also need the header files that are then passed to jextract.


   If you already have Visual Studio installed with Windows SDK (default if you checked `Desktop development with C++`
when installing VS), you can skip this dependency.


   Otherwise, you either have to install Visual Studio with the `Desktop development with C++` workload, or
download the Windows SDK:

   1. Go to https://developer.microsoft.com/en-us/windows/downloads/windows-sdk/
   2. Click the `DOWNLOAD THE INSTALLER` button
   3. Run the installer, and make sure the `Install the Windows Software Development Kit - ...` option is checked, then click Next.
   4. In the next page, you can select `No` to turn off telemetry (recommended)
   5. Accept the license agreement
   6. At the feature selection page, first, unchecked everything, then check the `Windows SDK for Desktop C++ amd64 Apps` option. It will automatically check other features, that is not a problem.
   7. Click Install, accept the UAC prompt, and wait for it to finish

3. ### At this point I recommend a system restart to make sure that every program has the correct system variables available.

## 2. Generating panama mappings
1. Run the `generate_win.bat` file. After it runs, the last line should say `Press any key to exit...`.

   Make sure the output of the script is EXACTLY the following:
```
    WARNING: Using incubator modules: jdk.incubator.jextract, jdk.incubator.foreign
    WARNING: Layout size not available for Policies
    WARNING: Layout size not available for Elements
    WARNING: Layout size not available for pEventLogRecords
    WARNING: Layout size not available for ulOffsets
    WARNING: Layout size not available for Format
    WARNING: Layout size not available for Lev1Depends
    WARNING: Layout size not available for Lev2Depends
    WARNING: skipping wcstold because of unsupported type usage: long double
    WARNING: skipping _wcstold_l because of unsupported type usage: long double
    WARNING: skipping .x because of unsupported type usage: long double
    WARNING: skipping strtold because of unsupported type usage: long double
    WARNING: skipping _strtold_l because of unsupported type usage: long double
    Press any key to exit...
```
If the output is different, there may be 3 reasons:
1. You installed project panama incorrectly. In this case it will say:
   ```
   'jextract' is not recognized as an internal or external command,
   operable program or batch file.
   Press any key to exit...
   ```
   Solution: Go through the [dependencies](#1-dependencies) again and make sure you verify that panama works.
2. You installed the Windows SDK incorrectly. In this case it will say either that `'Windows.h' file not found`, or `'d3d11.h' file not found`.
   Solution: Go through the [dependencies](#1-dependencies) again and make sure you verify that the Windows SDK is installed correctly. As a last resort, you can install Visual Studio 2019.
3. Something else:
   What is most likely in this case is that either a new Windows SDK or Project Panama version has released. Please check the open issues, and if there's no issue with your specific script output, please open a issue if the specific output hasn't been reported yet.

## 3. Generating wrappers
After the panama mappings have been generated, generating the wrapper classes is recommended.

These wrapper classes simplify interaction with structs and COM objects, and also turn many `#define`s into `public static final` variables, which can then be used in switch statements (`jextract` puts `#define`s into getter methods, which cannot be used with switch statements).

To do this, simply run `mvn clean compile exec:exec` and wait until it finishes.

Wrapper classes are just the original struct/COM interface name with a `_J` suffix.

Extracted defines are placed in the `win32.mapped` package, separated by category using classes with descriptive names.

## 4. Installing to local maven repository
At this point, you can install the mappings to your local maven repo by running `mvn install`,  however, make sure you DON'T run the `clean` phase, because then maven will have to recompile the panama mappings again.

Because Project Panama is still a java incubator feature, and it requires extensive manual configuration of the system to run properly, no precompiled binaries will be distributed. By manually compiling the library yourself, you also make sure that your system is set up properly.

After installing, you can use the project as a regular maven dependency in your project.

## 5. Running code with panama
By default, foreign (native) access, even in Project Panama, is blocked. To solve this, you have to provide the following command line arguments to java when running programs that contain panama code: `-Dforeign.restricted=permit --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.foreign`

You can inspect this project's pom.xml to see how to do that with maven. 

## 6. Linking libraries
Panama does NOT link code by default, so for any external libraries that you want to use, you first need to load the appropriate libraries.

For that you need to use java's built-in `System.loadLibrary(...);` call. In short: You either have to have the dll/so file in PATH, or in the working directory, and invoke `System.loadLibrary(<library>);`, where `<library>` is the library's filename without the .so/.dll extension.

Win32 specific info:

To figure out which libraries you need for win32 functions, look up the function on MSDN (msdn is well indexed by most web search engines, it's enough if you type `msdn FunctionName` in your preferred search engine, and the first search result will usually get you there), scroll to the bottom, and under the `Requirements` header, you can find the library in the `DLL` row.

With panama there are no function-style defines, so you need to use either the narrow (A) or wide(W) character-based functions. I recommend the narrow versions, as they map nicely to UTF-8 strings.

Common example: You want to create a win32 window. For that you need the `WNDCLASSEXA` struct (library independent), retrieved with `GetModuleHandleA`, and the `CreateWindowExA` function. These two functions are implemented in:

`GetModuleHandleA`: `Kernel32.dll`

`CreateWindowExA`: `User32.dll`

So, before calling any of these, preferably right at the start of your main function, or better yet, in a `static{}` block in your startup class, you would load those libraries like this:
```
System.loadLibrary("Kernel32");
System.loadLibrary("User32");
```
Note the lack of file extensions.

## 7. What next?
Over the course of the next few months I will continue polishing the generator code and extract frequently used defines into wrapper classes to improve their usage in switch statements even further.
I also want to fix the main drawback of struct wrappers, which is: if you have a struct composed of smaller sub-structs (for example, `DXGI_SWAP_CHAIN_DESC`), the wrapper library does create the getters/setters properly, so you have to fall back to the pure panama mappings.

## 8. Inspiration
I originally came up with this idea after having a small spike of interest in direct3D, but found C++ programming to not be my cup of tea.
I wanted to do direct3d stuff in java, but no matter how much I looked, I couldn't find _any_ up-to-date wrapper for direct3d and win32, other than the basic win32 mappings inside LWJGL, which, not surprisingly, don't contain D3D stuff.

Then, I stumbled Project Panama, and after studying it and a few weeks of experiments, I decided to take matters into my own hands.

A bit of lore between MS and Java, I found while researching:

While scouting the web for COM implementations in java, i stumbled upon an article mentioning an abandoned programming language called J++, and lawsuit between Sun Microsystems and Microsoft, which eventually lead to the creation of the .NET framework and the C# programming language.
So it seems like this project brings Java and Microsoft back together, a bit like how it was 20 years ago.

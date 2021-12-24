# JWin32
A Project Panama-based mapping + wrapper generator for win32 headers.
## IMPORTANT
* ### As of 0.5.0, no prebuilt jars will be provided. You must generate the mappings yourself.
  This is due to the new modular header system, which makes it impractical to include prebuilt jars for every combination.


* Now accepting pull requests for implementing function macros. Jextract cannot extract these, so it requires manual
  labor. See the [Macros file](https://github.com/FalsePattern/jwin32/blob/master/src/main/java/com/falsepattern/jwin32/Macros.java)
  for examples.

## Table of Contents
0. [Disclaimer](#0-disclaimer)
1. [What is Project Panama](#1-what-is-project-panama)
2. [Dependencies](#2-dependencies)
3. [Selecting libraries](#3-selecting-libraries-to-be-made-available-in-java)
4. [Generating mappings, wrappers and jar](#4-generating-mappings-wrappers-and-jar)
5. [Running code with panama](#5-running-code-with-panama)
6. [Linking libraries](#6-linking-libraries)
7. [What next?](#7-what-next)
8. [Inspiration](#8-inspiration)
9. [Changelog](#9-changelog)

## 0. Disclaimer
This project is not endorsed by nor affiliated with either Microsoft, Oracle, nor the OpenJDK project.

MSVC, Windows.h, d3d11.h and the Windows SDK are owned by Microsoft, and due to licensing issues, are not included as a
part of this source code.

The project source code does not include generated and mapped files because there's a lot of files (more than 10k after
every class is generated)

## 1. What is Project Panama
Java's project panama is an incubating feature of java, only accessible on a specific branch, is a brand-new solution
that gets rid of writing JNI or JNA wrappers for native libraries, and instead almost completely automates the process,
or hides it behind neat and efficient wrapper classes.
* Project Panama: https://openjdk.java.net/projects/panama/
* Project Panama Builds: https://jdk.java.net/panama/

Important reading before experimenting:
* handling native memory: https://github.com/openjdk/panama-foreign/blob/foreign-jextract/doc/panama_memaccess.md
* handling native functions: https://github.com/openjdk/panama-foreign/blob/foreign-jextract/doc/panama_ffi.md

## 2. Dependencies
0. ### (Optional) Installing the Windows developer VM.
   There's a possibility of jextract failing due to a nonstandard system setup. In this case, you need to do the
   jextract phase inside the Windows 10 developer VM, or if you don't want to install the Windows sdk on your host OS.
   You still need the panama jdk on your host to run the rest of the steps.
   1. You need either VMWare, Hyper-V, VirtualBox, or Parallels. If you haven't installed one of those yet, VirtualBox
      is the recommended option, as it was the only one tested with this project: https://www.virtualbox.org/wiki/Downloads
   2. Download the Windows 10 developer VM image for you virtual machine: https://developer.microsoft.com/en-us/windows/downloads/virtual-machines/
   3. (VirtualBox only) Extract the WinDev....Eval.ova file from the archive you downloaded, inside VirtualBox, click on `Import`,
      select the ova file in the `File` box, click `next`, then `Import`. After that, virtualbox will begin importing the image.
   4. Start the developer virtual machine, download this repository inside it, then proceed with the following steps.
   
1. ### Project Panama
   If you're using the VM, you still need to install this both on the host and the VM.
   1. Download and extract JDK-panama from this URL: https://jdk.java.net/panama

       (The latest version at the time of writing is `17-panama+3-167`)
   2. Next, you have to replace your currently installed java with Panama in the PATH and JAVA_HOME system variables:
     
       Inside PATH, replace your current java path with, or, if you don't have one, add: `<Directory where you extracted panama>\jdk-17-panama\bin`
     
       Set JAVA_HOME to: `<Directory where you extracted panama>\jdk-17-panama`
   3. To verify that panama has been added to PATH properly, open a command prompt, and type `java - version`. 
   The first output line should start with `openjdk version "17-panama"`.
   4. Also enter `jextract -version`into the same command prompt. If the output starts with
      `WARNING: Using incubator modules: jdk.incubator.foreign, jdk.incubator.jextract`, panama is correctly
      installed.
2. ### Maven (optional if your IDE doesn't have it, or you don't use and IDE)
   Note: You only need this on your host system, not inside the VM.

   1. Download and extract maven from this URL: https://maven.apache.org/download.cgi
   2. Add the maven bin directory to your PATH: `<Directory where you extracted maven>\bin`
   3. To verify that maven has been added to PATH properly, open a command prompt, and type `mvn -version`.
   4. If the output starts with `Apache Maven`, maven is correctly installed.
4. ### Windows SDK
   After you've installed project panama, you also need the header files that are then passed to jextract.
   If you're using the VM for the jextract step, you don't need the SDK on your host system, only on the VM.


   If you already have Visual Studio installed with Windows SDK (default if you checked `Desktop development with C++`
when installing VS), you can skip this dependency.


   Otherwise, you either have to install Visual Studio with the `Desktop development with C++` workload.

   If you don't have VS installed yet:
   1. Go to https://visualstudio.microsoft.com/downloads/
   2. Scroll down to Visual Studio 2019
   3. Under Community, click the `Free download` button.
   4. Start the installer, and follow the instructions until you reach the workload selection screen.

   If you already have VS installed, just type visual studio installer in the start menu search and launch it. Then,
   click Modify to enter the workload selection menu.

   The following steps are the same for both:
   1. Select the `Desktop development with C++` workload
   2. Click install and wait until it finishes (Approximately 2 GB download size, 8 GB on disk. Less if visual studio
      is already installed)

   ### At this point, restart your system to make sure that every program has the correct system variables available. (when running the VM, only restart the VM).

## 3. Selecting libraries to be made available in java
First off, go into the `c` directory, and open the `native.h` file. In this file you can configure what you want to include.

Currently, you can select the following:
* Windows.h configurations using `#define NO...` defines.
* Direct3D version. You can choose any combination of directx 9, 10, 11, or 12. You can also choose none of them.
* DXGI

## 4. Generating mappings, wrappers and jar
After 0.5.0, the build script was unified, and the entire process is now done in one go.

The `build_install.bat` script installs the compiled jar files into the local maven repository.

The `build_package.bat` script outputs the compiled jar files into the `target` directory.

You can safely ignore errors that match one of these examples:
* `WARNING: Using incubator modules: jdk.incubator.jextract, jdk.incubator.foreign`
* `WARNING: Layout size not available for ...`
* `WARNING: skipping ... because of unsupported type usage: long ...`

You can check the full list of all known non-issue warnings in `JEXTRACT_WARNINGS.txt`   

If you see any other kinds of errors, go back to the dependency setup step, download the Windows 10 developer virtual
machine, and redo the setup inside it. If it fails in the virtual machine too, only then should you create a new
issue concerning this step.

The wrapper classes simplify interaction with structs and COM objects, and also turn many `#define`s
into `public static final` variables, which can then be used in switch statements
(`jextract` puts `#define`s into getter methods, which cannot be used with switch statements).

To do this, simply run one of the `build_` batch files and wait until it finishes.

Wrapper classes are just the original struct/COM interface name with a `_J` suffix.

Wrapper code is placed in the `win32.mapped` package, more precisely, structs get put in `win32.mapped.struct`,
COM interfaces in `win32.mapped.com`, and `#define`d constants in the `win32.mapped.constants.Constants` class.

## 5. Running code with panama
By default, foreign (native) access, even in Project Panama, is blocked. To solve this, you have to provide the
following command line arguments to java when running programs that contain panama code:
`-Dforeign.restricted=permit --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.foreign`

You can inspect this project's pom.xml to see how to do that with maven. 

## 6. Linking libraries
Panama does NOT link code by default, so for any external libraries that you want to use, you first need to load the
appropriate libraries.

For that you need to use java's built-in `System.loadLibrary(...);` call. In short: You either have to have the dll/so
file in PATH, or in the working directory, and invoke `System.loadLibrary(<library>);`, where `<library>` is the
library's filename without the .so/.dll extension.

Win32 specific info:

To figure out which libraries you need for win32 functions, look up the function on MSDN (msdn is well indexed by most
web search engines, it's enough if you type `msdn FunctionName` in your preferred search engine, and the first search
result will usually get you there), scroll to the bottom, and under the `Requirements` header, you can find the library
in the `DLL` row.

With panama there are no function-style defines, so you need to use either the narrow (A) or wide(W) character-based
functions. I recommend the narrow versions, as they map nicely to UTF-8 strings.

Common example: You want to create a win32 window. For that you need the `WNDCLASSEXA` struct (library independent),
retrieved with `GetModuleHandleA`, and the `CreateWindowExA` function. These two functions are implemented in:

`GetModuleHandleA`: `Kernel32.dll`

`CreateWindowExA`: `User32.dll`

So, before calling any of these, preferably right at the start of your main function, or better yet, in a `static{}`
block in your startup class, you would load those libraries like this:

```
System.loadLibrary("Kernel32");
System.loadLibrary("User32");
```
Note the lack of file extensions.

## 7. What next?
Over the course of the next few months I will continue polishing the generator code and add more quality of life wrappers.

Goals:
- [x] Create wrapper code for structs inside structs
- [ ] Create wrapper code for arrays inside structs
- [x] Create wrapper classes for constant defines

## 8. Inspiration
I originally came up with this idea after having a small spike of interest in direct3D, but found C++ programming to
not be my cup of tea.
I wanted to do direct3d stuff in java, but no matter how much I looked, I couldn't find _any_ up-to-date wrapper for
direct3d and win32, other than the basic win32 mappings inside LWJGL, which, not surprisingly, don't contain D3D stuff.

Then, I stumbled Project Panama, and after studying it and a few weeks of experiments, I decided to take matters into
my own hands.

A bit of lore between MS and Java, I found while researching:

While scouting the web for COM implementations in java, i stumbled upon an article mentioning an abandoned programming
language called J++, and lawsuit between Sun Microsystems and Microsoft, which eventually lead to the creation of the
.NET framework and the C# programming language.

## 9. Changelog
### [0.5.0] - 2021-12-24
#### Added
* String and pointer constant extraction
* Internal code encapsulation using project jigsaw's module-info.java system
* Bat files for the compile step
* Static wrapper methods for MemoryUtil singleton's MemoryAllocator
* Copyright notices to all source files and build scripts
* Some basic win32 macros.
* Special logic for GUIDs, for simpler creation of GUID structs used in COM.
* Added DXGI to the config file

#### Changed
* C headers are now modularized, and can be configured by the user before jextract-ing.
* Reworked guide to make the jextract step reproducible in a vm.
* Moved banned classes list to external file for better editing.
* Separated the cleanup script from the generator.

#### Removed
* Maven distribution management
* Security vulnerability caused by unicode file encoding. ASCII is now the default.

### [0.4.2] - 2021-10-17
#### Added
* Additional Direct3D headers based on the [api docs](https://github.com/MicrosoftDocs/sdk-api/blob/docs/sdk-api-src/content/_direct3d11/index.md).
  
  Note: this does not include the C++ interoperability header, as jextract only translates C headers.
  * d3d11_1.h
  * d3d11_2.h
  * d3d11_3.h
  * d3d11_4.h
  * d3d11sdklayers.h
  * d3d11shader.h
  * d3d11shadertracing.h
  * d3dcommon.h
  * d3dcsx.h
* Constant extractor now extracts byte, short, int, long, float, and double constants. String constants are not extracted for now.
#### Changed
* Constant extractor now also removes the respective getter methods from Win32.java and its superclasses to fix some potential confusion.
### [0.4.1] - 2021-10-16
#### Added
* Javadoc to the memory manipulation classes
#### Changed
* Fixed some broken logic in `MemoryUtil`
#### Removed
* Testing code in `MemoryStack`
### [0.4.0] - 2021-10-15
#### Added
* Memory manipulation helper classes inside `com.falsepattern.jwin32.memory`
#### Changed
* Moved code generation stuff into "internal" class to signal to developers to not use those
* Reworked COM wrappers to use MemoryAddress instead of MemorySegment in the constructor argument, saving some client-side conversion
### [0.3.1] - 2021-10-15
#### Changed
* Now generates less whitespace into wrapper classes
### [0.3.0] - 2021-10-15
#### Added
* Extraction of constants (WM_..., CS_..., etc.)
  * They were already available through the pure mappings, however, they were hidden behind getter functions, and thus,
    you couldn't use them in switch statements. Now you can. 
  * There's about 27k of them, extracted to `win32.mapped.Constants`
  
### [0.2.0] - 2021-10-14
#### Added
* Automatic fixing for known issues in:
  * ID3DInclude
  * XMLDOMDocumentEvents
#### Changed
* Reimplemented struct generator. Nested structs are now supported.
#### Removed
* The following win32 structs will not have wrappers generated, due to alignment incompatibility:
  * _MMIOINFO
  * DRVCONFIGINFOEX
  * IMAGE_AUX_SYMBOL_TOKEN_DEF
  * tagDRVCONFIGINFO
  * midihdr_tag
  * tagMCI_ANIM_OPEN_PARMSA
  * tagMCI_ANIM_OPEN_PARMSW
  * tagMCI_ANIM_WINDOW_PARMSA
  * tagMCI_ANIM_WINDOW_PARMSW
  * tagMCI_BREAK_PARMS
  * tagMCI_OPEN_PARMSA
  * tagMCI_OPEN_PARMSW
  * tagMCI_OVLY_OPEN_PARMSA
  * tagMCI_OVLY_OPEN_PARMSW
  * tagMCI_OVLY_WINDOW_PARMSA
  * tagMCI_OVLY_WINDOW_PARMSW
  * tagMCI_WAVE_OPEN_PARMSA
  * tagMCI_WAVE_OPEN_PARMSW
  * tagMETAHEADER
  * tagMIXERLINEA
  * tagMIXERLINECONTROLSA
  * tagMIXERLINECONTROLSW
  * tagMIXERLINEW
  * tagBITMAPFILEHEADER
  * tMIXERCONTROLDETAILS

    including all structs derived from these.
  
    If a struct contains one of these as a sub-struct, that sub-struct will not receive a wrapper field.

### [0.0.0-0.1.1]
* Extremely early code, no change tracking was done

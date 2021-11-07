@echo off
echo Cleaning up folders...
rmdir /S /Q .\src\main\java\win32
del .\src\main\java\module-info.java
echo Creating empty folders...
mkdir .\src\main\java\win32
mkdir .\src\main\java\win32\pure
mkdir .\src\main\java\win32\mapped
mkdir .\src\main\java\win32\mapped\com
mkdir .\src\main\java\win32\mapped\struct
mkdir .\src\main\java\win32\mapped\constants
echo Cleanup finished!
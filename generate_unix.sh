#!/usr/bin/env bash
rm -rf ./src/main/java/win32
mkdir ./src/main/java/win32
mkdir ./src/main/java/win32/pure
mkdir ./src/main/java/win32/mapped
mkdir ./src/main/java/win32/mapped/com
mkdir ./src/main/java/win32/mapped/struct

#Edit this line if you want to make custom mappings:
jextract --source --header-class-name Win32 -d ./src/main/java -t win32.pure ./c/native.h

# shellcheck disable=SC2162
read -p "Press any key to exit..."
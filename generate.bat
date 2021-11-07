@echo off
call .\cleanup.bat
echo Generating panama mappings...
REM Edit this line if you want to make custom mappings:
jextract --source --header-class-name Win32 -d .\src\main\java -t win32.pure .\c\native.h

echo Generation finished!
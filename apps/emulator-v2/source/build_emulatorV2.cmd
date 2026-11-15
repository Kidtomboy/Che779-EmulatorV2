@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul
cd /d "%~dp0"

REM ============================================================
REM Che779 EmulatorV2 1.2.0 - Windows build
REM Requires JDK 8+ (JAVA_HOME or javac/jar in PATH)
REM Output: Che779_EmulatorV2.jar (cung thu muc voi script)
REM ============================================================

if defined JAVA_HOME (
    set "JAVAC=%JAVA_HOME%\bin\javac.exe"
    set "JAR=%JAVA_HOME%\bin\jar.exe"
) else (
    set "JAVAC=javac"
    set "JAR=jar"
)

"%JAVAC%" -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Cannot find javac. Install JDK 8+ and configure JAVA_HOME or PATH.
    goto :err
)

if not exist "stubs\" (
    echo [ERROR] Missing stubs directory.
    goto :err
)
if not exist "src\" (
    echo [ERROR] Missing src directory.
    goto :err
)
if not exist "res\" (
    echo [ERROR] Missing res directory.
    goto :err
)
if not exist "lib\asm-3.2.jar" (
    echo [ERROR] Missing lib\asm-3.2.jar.
    goto :err
)

echo [CLEAN] Removing previous build folders...
for %%D in (out stubout jarroot) do (
    if exist "%%D\" rd /s /q "%%D" 2>nul
    if exist "%%D\" (
        echo [ERROR] Cannot remove %%D. Close programs using files in that folder and retry.
        goto :err
    )
)

if exist "stubs.txt" del /q "stubs.txt"
if exist "srcs.txt" del /q "srcs.txt"

mkdir out
if errorlevel 1 goto :err
mkdir stubout
if errorlevel 1 goto :err
mkdir jarroot
if errorlevel 1 goto :err

REM Create javac response files. Forward slashes avoid Windows backslash parsing issues.
echo [LIST] Collecting Java source files...
> "stubs.txt" (
    for /r "stubs" %%F in (*.java) do (
        set "P=%%~fF"
        echo "!P:\=/!"
    )
)
> "srcs.txt" (
    for /r "src" %%F in (*.java) do (
        set "P=%%~fF"
        echo "!P:\=/!"
    )
)

for %%A in ("stubs.txt") do if %%~zA EQU 0 (
    echo [ERROR] No Java files found in stubs.
    goto :err
)
for %%A in ("srcs.txt") do if %%~zA EQU 0 (
    echo [ERROR] No Java files found in src.
    goto :err
)

echo [1/5] Compiling stubs...
"%JAVAC%" -source 8 -target 8 -nowarn -encoding UTF-8 -d "stubout" @"stubs.txt"
if errorlevel 1 goto :err

echo [2/5] Compiling application sources...
"%JAVAC%" -source 8 -target 8 -nowarn -proc:none -encoding UTF-8 -d "out" -cp "lib\asm-3.2.jar;stubout" @"srcs.txt"
if errorlevel 1 goto :err

echo [3/5] Collecting classes and resources...
xcopy "out\*" "jarroot\" /e /i /y /q >nul
if errorlevel 2 goto :err
xcopy "res\*" "jarroot\" /e /i /y /q >nul
if errorlevel 2 goto :err

pushd "jarroot"
"%JAR%" xf "..\lib\asm-3.2.jar"
if errorlevel 1 (
    popd
    goto :err
)
if exist "META-INF\" rd /s /q "META-INF"
popd

echo [4/5] Creating manifest...
> "manifest.txt" echo Manifest-Version: 1.0
>>"manifest.txt" echo Main-Class: com.che779.emu.Che779Main
>>"manifest.txt" echo Class-Path: lib/jna.jar
>>"manifest.txt" echo Implementation-Title: Che779 EmulatorV2
>>"manifest.txt" echo Implementation-Version: 1.2.0
>>"manifest.txt" echo Created-By: Che779 Team

echo [5/5] Packaging JAR...
set "OUTPUT=%~dp0Che779_EmulatorV2.jar"

if exist "%OUTPUT%" (
    del /f /q "%OUTPUT%" >nul 2>&1
)
if exist "%OUTPUT%" (
    echo [ERROR] Output JAR is locked. Close Che779 EmulatorV2 and related java.exe processes.
    goto :err
)

"%JAR%" cfm "%OUTPUT%" "manifest.txt" -C "jarroot" .
if errorlevel 1 goto :err

if not exist "%OUTPUT%" (
    echo [ERROR] JAR command finished but output file was not created.
    goto :err
)

del /q "stubs.txt" "srcs.txt" >nul 2>&1
echo.
echo [SUCCESS] Build completed:
echo "%OUTPUT%"
if not defined CI pause
exit /b 0

:err
echo.
echo [ERROR] Build failed. No success message was issued.
if not defined CI pause
exit /b 1
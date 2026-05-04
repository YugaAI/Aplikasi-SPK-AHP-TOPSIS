@echo off
echo ============================================
echo   Building SPK AHP+TOPSIS Application
echo ============================================
echo.

set "JAVA_HOME=C:\Program Files\Java\jdk1.8.0_341"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [1/3] Cleaning output directory...
if exist "out" rmdir /s /q "out"
mkdir "out"

echo [2/3] Compiling Java sources...
setlocal enabledelayedexpansion
set "SOURCES="
for /r "src" %%f in (*.java) do (
    set "SOURCES=!SOURCES! "%%f""
)

javac -encoding UTF-8 -d "out" -sourcepath "src" -cp "lib\sqlite-jdbc.jar;lib\jbcrypt.jar" %SOURCES%

if errorlevel 1 (
    echo.
    echo   BUILD FAILED!
    pause
    exit /b 1
)
endlocal

echo [3/3] Copying resources...
if not exist "out\com\spk\presentation\styles" mkdir "out\com\spk\presentation\styles"
copy /y "src\com\spk\presentation\styles\neumorphism.css" "out\com\spk\presentation\styles\" >nul 2>&1

echo.
echo ============================================
echo   BUILD SUCCESSFUL!
echo   Run with: run.bat
echo ============================================
echo.

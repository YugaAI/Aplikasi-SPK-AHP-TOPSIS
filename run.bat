@echo off
echo ============================================
echo   SPK AHP+TOPSIS Application
echo ============================================
echo.

set "JAVA_HOME=C:\Program Files\Java\jdk1.8.0_341"
set "PATH=%JAVA_HOME%\bin;%PATH%"

java -cp "out;lib\sqlite-jdbc.jar;lib\jbcrypt.jar" com.spk.presentation.MainApp %*

if errorlevel 1 (
    echo.
    echo Application exited with error.
    pause
)

@echo off
title Outrix ERP Launcher
echo ============================================================
echo         📦 OUTRIX ERP INVENTORY MANAGEMENT SYSTEM
echo ============================================================
echo.

:: Check if JAR exists
if not exist "target\outrix-erp-1.0.0.jar" (
    echo [INFO] Packaged JAR not found. Re-building the project first...
    echo.
    goto BUILD
)
goto RUN

:BUILD
:: Check if Maven is available in PATH
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    set MAVEN_CMD=mvn
    goto DO_BUILD
)

:: Try bundled IntelliJ Maven
set BUNDLED_MAVEN="C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\plugins\maven\lib\maven3\bin\mvn.cmd"
if exist %BUNDLED_MAVEN% (
    set MAVEN_CMD=%BUNDLED_MAVEN%
    goto DO_BUILD
)

echo [ERROR] Maven was not found on your system.
echo Please build the project inside your IDE (IntelliJ) first.
pause
exit /b 1

:DO_BUILD
:: Set Java Home to bundled JBR if not already set or java is missing from path
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\jbr" (
        set JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\jbr
    )
)
echo [INFO] Executing Maven build...
call %MAVEN_CMD% clean package
if %errorlevel% neq 0 (
    echo [ERROR] Build failed.
    pause
    exit /b 1
)
echo.

:RUN
:: Determine Java executable
set JAVA_EXE=java
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\jbr\bin\java.exe" (
        set JAVA_EXE="C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\jbr\bin\java.exe"
    ) else (
        echo [ERROR] Java was not found on your system.
        pause
        exit /b 1
    )
)

echo [INFO] Starting Outrix ERP...
start "" %JAVA_EXE% -jar "target\outrix-erp-1.0.0.jar"
exit /b 0

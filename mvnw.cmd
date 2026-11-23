@REM =====================================================================
@REM  Maven Wrapper (Windows) - Descarga Apache Maven si no esta presente
@REM  y ejecuta Maven sobre el proyecto actual. No requiere instalar Maven.
@REM =====================================================================
@echo off
setlocal enabledelayedexpansion

set "MAVEN_VERSION=3.9.6"
set "MVNW_DIR=%~dp0"
set "MVNW_DIR=%MVNW_DIR:~0,-1%"
set "MAVEN_HOME=%MVNW_DIR%\.mvn\apache-maven-%MAVEN_VERSION%"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo [mvnw] Apache Maven %MAVEN_VERSION% no encontrado. Descargando...
    if not exist "%MVNW_DIR%\.mvn" mkdir "%MVNW_DIR%\.mvn"
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile '%MVNW_DIR%\.mvn\maven.zip'"
    if errorlevel 1 (
        echo [mvnw] ERROR: no se pudo descargar Maven. Verifica tu conexion a Internet.
        exit /b 1
    )
    echo [mvnw] Extrayendo Maven...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%MVNW_DIR%\.mvn\maven.zip' -DestinationPath '%MVNW_DIR%\.mvn' -Force"
    del /q "%MVNW_DIR%\.mvn\maven.zip"
)

set "JAVA_HOME=%JAVA_HOME%"
call "%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal

@echo off
setlocal enabledelayedexpansion

set "TYPE=%~1"
if "%TYPE%"=="" set "TYPE=app-image"

where jpackage >nul 2>nul
if errorlevel 1 (
  echo jpackage nicht gefunden. Bitte JDK 17+ installieren bzw. JAVA_HOME korrekt setzen.
  exit /b 1
)

echo [1/3] Baue Projekt...
call mvn -DskipTests clean package
if errorlevel 1 exit /b 1

set "APP_JAR="
set "APP_JAR_NAME="
for %%F in ("target\*-all.jar") do (
  set "APP_JAR=%%~fF"
  set "APP_JAR_NAME=%%~nxF"
)

if "%APP_JAR%"=="" (
  echo Kein fat JAR gefunden. Build fehlgeschlagen?
  exit /b 1
)

if not exist dist mkdir dist

echo [2/3] Erzeuge Windows-Paket vom Typ %TYPE%...
jpackage ^
  --type %TYPE% ^
  --name "QR Code Generator" ^
  --input target ^
  --main-jar "%APP_JAR_NAME%" ^
  --main-class de.mf.qr.QrGeneratorApp ^
  --app-version 1.0.0 ^
  --vendor "MF" ^
  --dest dist ^
  --win-shortcut ^
  --win-menu

if errorlevel 1 (
  echo Paket-Erstellung fehlgeschlagen.
  echo Tipp: Fuer --type msi/--type exe wird ggf. WiX benoetigt.
  exit /b 1
)

echo [3/3] Fertig. Ausgabe unter .\dist
echo Du kannst den Typ optional angeben: build-windows-app.bat app-image ^| msi ^| exe
exit /b 0

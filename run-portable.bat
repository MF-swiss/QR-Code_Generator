@echo off
setlocal enabledelayedexpansion

set "APP_JAR="
for %%F in ("target\*-all.jar") do (
  set "APP_JAR=%%~fF"
)

if "%APP_JAR%"=="" (
  echo Kein portable JAR gefunden. Bitte zuerst bauen (mvn clean package).
  exit /b 1
)

echo Starte %APP_JAR%
start "QR Code Generator" javaw -jar "%APP_JAR%"
exit /b 0

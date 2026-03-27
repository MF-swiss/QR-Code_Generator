@echo off
REM === QR-Code Generator als Windows-Installer bauen (jpackage) ===
REM Voraussetzungen: Java 17+ und JavaFX SDK, jpackage im JDK enthalten

REM Pfad zum JavaFX SDK (ggf. anpassen)
set JAVAFX_SDK=javafx-sdk-27

REM Prüfen, ob JavaFX SDK vorhanden ist
if not exist "%JAVAFX_SDK%\lib" (
    echo JavaFX SDK nicht gefunden! Bitte lade es von https://gluonhq.com/products/javafx/ herunter und entpacke es nach %JAVAFX_SDK%.
    pause
    exit /b 1
)

REM Projekt bauen
mvn clean package

REM jpackage ausführen
jpackage ^
  --type exe ^
  --input target ^
  --name QRCodeGenerator ^
  --main-jar qr-code-generator-1.0.0-all.jar ^
  --main-class de.mf.qr.QrGeneratorApp ^
  --java-options "--module-path %JAVAFX_SDK%\lib --add-modules javafx.controls" ^
  --icon src\main\resources\icon.ico

pause
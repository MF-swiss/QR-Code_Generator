@echo off
REM === QR-Code Generator mit JavaFX starten (portable, keine Installation nötig) ===

REM Pfad zum JavaFX SDK (ggf. anpassen, z.B. javafx-sdk-27)
set JAVAFX_SDK=javafx-sdk-27

REM Prüfen, ob JavaFX SDK vorhanden ist
if not exist "%JAVAFX_SDK%\lib" (
    echo JavaFX SDK nicht gefunden! Bitte lade es von https://gluonhq.com/products/javafx/ herunter und entpacke es nach %JAVAFX_SDK%.
    pause
    exit /b 1
)


REM Starte das Programm
java --module-path "%JAVAFX_SDK%\lib" --add-modules javafx.controls -jar target\qr-code-generator-1.0.0-all.jar

pause
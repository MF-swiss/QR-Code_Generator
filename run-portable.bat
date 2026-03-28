@echo off
set JAVA_EXE=java
set JAVAFX_SDK=javafx-sdk-27
%JAVA_EXE% --module-path %JAVAFX_SDK%\lib --add-modules javafx.controls,javafx.fxml -jar target\qr-code-generator-1.0.0-all.jar
pause

# QR Code Generator

Desktop-App zum Erstellen von QR-Codes (YouTube, Webseite, Text, WLAN, Standort).

## Schnellstart (einfach abrufen / starten)

### Option A: Portable starten (ohne Installation)

1. Projekt bauen (erzeugt ein fat JAR):
   - `mvn clean package`
2. Danach starten:
   - Doppelklick auf `run-portable.bat`

Die App startet dann direkt als Desktop-Fenster.

## Als installierbare Windows-App bauen

### Option B: Installierbares Paket

1. Sicherstellen, dass verfügbar ist:
   - JDK 17+ (mit `jpackage`)
   - Maven
2. Paket bauen:
   - Doppelklick auf `build-windows-app.bat`

Standard ist `app-image` (portable App-Ordner in `dist/`).

Optional kannst du in der Konsole einen Typ angeben:
- `build-windows-app.bat app-image`
- `build-windows-app.bat msi`
- `build-windows-app.bat exe`

> Hinweis: Für `msi`/`exe` wird unter Windows je nach JDK/Setup ggf. WiX benötigt.

## Ausgabe

- Portable JAR: `target/*-all.jar`
- Windows-Paket: `dist/`

## Entwicklung

Starten im Dev-Modus:
- `mvn javafx:run`

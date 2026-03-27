# QR Code Generator

[![Build and Release Artifacts](https://github.com/MF-swiss/QR-Code_Generator/actions/workflows/build-release.yml/badge.svg)](https://github.com/MF-swiss/QR-Code_Generator/actions/workflows/build-release.yml)
[![Latest Release](https://img.shields.io/github/v/release/MF-swiss/QR-Code_Generator?display_name=tag)](https://github.com/MF-swiss/QR-Code_Generator/releases)

Desktop-App zum Erstellen von QR-Codes (YouTube, Webseite, Text, WLAN, Standort).

## Qualität & Professionalität

- Live-Vorschau während der Eingabe
- Echtzeit-Validierung mit klaren Hinweisen
- Export als PNG und SVG (inkl. transparentem Hintergrund)
- Optionales Logo in der QR-Mitte (auch im SVG-Export eingebettet)

## Datenschutz

Alle Eingaben werden lokal verarbeitet. Es werden keine Inhalte an einen Cloud-Dienst übertragen.

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

## CI / Build-Artefakte

Eine GitHub-Action (`.github/workflows/build-release.yml`) baut automatisch bei Push/PR:

- Maven-Paket (`target/*.jar`)
- Windows-App-Image (`dist/`, sofern `jpackage` verfügbar)

Die Artefakte werden im jeweiligen Workflow-Run hochgeladen.

## Versionierung & Releases (SemVer)

Für Releases werden semantische Tags genutzt, z. B.:

- `v1.0.0`
- `v1.1.0`
- `v1.1.1`

Sobald ein Tag mit Muster `v*` gepusht wird, passiert automatisch:

1. Build läuft durch
2. Artefakte werden erzeugt/gesammelt
3. GitHub Release wird erstellt
4. Changelog/Release Notes werden automatisch generiert

Beispielablauf:

- `git tag v1.0.1`
- `git push origin v1.0.1`

## Ausgabe

- Portable JAR: `target/*-all.jar`
- Windows-Paket: `dist/`

## Entwicklung

Starten im Dev-Modus:
- `mvn javafx:run`

## Support / Version

- App-Version wird in der Oberfläche angezeigt ("Version x.y.z")
- Über-Dialog in der App liefert Funktionsübersicht

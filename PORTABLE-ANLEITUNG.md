# Anleitung: QR-Code Generator als portable App mit JavaFX

## 1. JavaFX SDK herunterladen
- Lade das passende JavaFX SDK (z. B. für Windows) von https://gluonhq.com/products/javafx/ herunter.
- Entpacke das ZIP in den Projektordner, sodass z. B. der Ordner `javafx-sdk-21.0.2` existiert.

## 2. Projekt bauen
Öffne ein Terminal im Projektordner und führe aus:

    mvn clean package

Dadurch wird die Datei `target/qr-code-generator-1.0.0-all.jar` erstellt.

## 3. Programm starten
Starte die Datei `run-portable-javafx.bat` per Doppelklick.

**Hinweis:**
- Es wird automatisch geprüft, ob das JavaFX SDK vorhanden ist.
- Die Nutzer müssen nur Java installiert haben (ab Java 17 empfohlen).
- Keine weitere Installation nötig!

## 4. Optional: Eigenes JavaFX SDK-Verzeichnis
Wenn du das JavaFX SDK woanders entpackst, passe die Variable `JAVAFX_SDK` in der Batch-Datei an.

---

**Noch einfacher geht es mit jpackage (Windows-Installer/EXE). Sag Bescheid, wenn du das möchtest!**

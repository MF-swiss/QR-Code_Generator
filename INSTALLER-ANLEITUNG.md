# Anleitung: Windows-Installer (EXE) mit jpackage erstellen

## Voraussetzungen
- Java 17 oder neuer (jpackage ist ab Java 14 enthalten)
- JavaFX SDK (z. B. javafx-sdk-21.0.2, https://gluonhq.com/products/javafx/)
- Maven

## Schritte
1. JavaFX SDK herunterladen und in den Projektordner entpacken (z. B. `javafx-sdk-21.0.2`).
2. Optional: Ein Icon als `src/main/resources/icon.ico` ablegen (oder Zeile im Skript entfernen).
3. Im Projektordner die Datei `build-windows-installer.bat` per Doppelklick ausführen.

Das Skript erledigt alles automatisch:
- Baut das Projekt
- Erstellt mit jpackage eine EXE-Datei (Installer) im Projektordner

**Fertig!**

Die Nutzer können die EXE einfach installieren und starten – Java und JavaFX sind dann direkt enthalten.

---

**Hinweis:**
- Das JavaFX SDK wird nicht mit in die EXE gepackt, sondern als Laufzeitmodul eingebunden.
- Für andere Plattformen (Mac/Linux) ist jpackage ebenfalls verfügbar, die Befehle unterscheiden sich leicht.

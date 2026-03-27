# QR-Code Generator – Portable Nutzung

## So funktioniert's für Endnutzer

1. **ZIP-Archiv bereitstellen:**
   - Lege folgende Dateien/Ordner in ein ZIP:
     - `qr-code-generator-1.0.0-all.jar` (aus `target/`)
     - `run-portable-javafx.bat`
     - Ordner `javafx-sdk-27` (JavaFX SDK, https://gluonhq.com/products/javafx/)
     - Optional: `icon.ico` (für Verknüpfungen)

2. **Nutzer entpackt das ZIP**
   - Der Nutzer entpackt alles in einen beliebigen Ordner.

3. **Starten**
   - Doppelklick auf `run-portable-javafx.bat` – das Programm startet sofort.

**Vorteile:**
- Keine Installation nötig
- Keine Adminrechte nötig
- JavaFX ist schon dabei
- Icon kann für Desktop-Verknüpfung genutzt werden

**Hinweis:**
- Java (JRE/JDK) muss auf dem Zielsystem installiert sein (ab Java 17).
- Das Icon ist für den Installer optional, kann aber für Verknüpfungen verwendet werden.

---

**Tipp:**
Du kannst auch eine portable Java-Laufzeit (JRE) beilegen, dann läuft es sogar ohne Java-Installation auf dem Zielsystem.

Fertig! So einfach können andere dein Programm nutzen.
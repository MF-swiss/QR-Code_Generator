# PowerShell-Skript: Portable ZIP für QR-Code Generator erstellen
#
# Dieses Skript kopiert alle nötigen Dateien in einen neuen Ordner und erstellt daraus ein ZIP-Archiv.
#
# Voraussetzungen:
# - Das JavaFX SDK liegt als Ordner "javafx-sdk-27" im Projektverzeichnis
# - Das JAR wurde gebaut: target\qr-code-generator-1.0.0-all.jar
# - Das Skript wird im Projektverzeichnis ausgeführt

$ErrorActionPreference = 'Stop'

$base = "$PSScriptRoot"
$zipName = "QRCodeGenerator-Portable.zip"
$targetDir = "$base\QRCodeGenerator-Portable"

# Ordner neu anlegen
if (Test-Path $targetDir) { Remove-Item $targetDir -Recurse -Force }
New-Item -ItemType Directory -Path $targetDir | Out-Null

# Dateien kopieren
Copy-Item "$base\run-portable-javafx.bat" $targetDir
Copy-Item "$base\PORTABLE-README.md" $targetDir -ErrorAction SilentlyContinue
Copy-Item "$base\icon.ico" $targetDir -ErrorAction SilentlyContinue
Copy-Item "$base\target\qr-code-generator-1.0.0-all.jar" $targetDir
Copy-Item "$base\javafx-sdk-27" $targetDir -Recurse

# ZIP erstellen
if (Test-Path "$base\$zipName") { Remove-Item "$base\$zipName" -Force }
Compress-Archive -Path "$targetDir\*" -DestinationPath "$base\$zipName"

Write-Host "FERTIG! Die Datei $zipName enthält alles für die portable Nutzung." -ForegroundColor Green

# Vorbereitung der Release-Signierung für den Play Store

Dieses Dokument beschreibt die Schritte, um die App mit deinem vorhandenen Keystore (`Plattendruck.jks`) zu signieren, damit ein Update im Play Store möglich ist.

## User Review Required

> [!IMPORTANT]
> **Keystore-Passwörter:** Ich werde die Konfiguration so vorbereiten, dass die App beim Bauen (Build) auf deinen Keystore zugreift. Aus Sicherheitsgründen solltest du Passwörter **niemals** direkt in den Code schreiben. Wir nutzen dafür eine separate Datei, die nicht in Git gespeichert wird.
> **Pfad zum Keystore:** Ich habe die Datei unter `/home/intux/Android Plattendruck App/Plattendruck.jks` gefunden.

## Proposed Changes

### 1. Konfigurationsdatei für Passwörter
#### [NEW] `keystore.properties` (im Projekt-Root)
Ich erstelle eine Vorlage für diese Datei. Du musst dort später deine echten Passwörter und den "Alias" (den Namen des Schlüssels im Keystore) eintragen.
```properties
storePassword=DEIN_PASSWORT
keyPassword=DEIN_PASSWORT
keyAlias=DEIN_ALIAS
storeFile=/home/intux/Android Plattendruck App/Plattendruck.jks
```

### 2. Anpassung der Build-Konfiguration
#### [MODIFY] [app/build.gradle](file:///home/intux/AndroidStudioProjects/Plattendruck/app/build.gradle)
- Hinzufügen einer Logik, die die `keystore.properties` Datei einliest.
- Definition eines `release` Signing-Configs.
- Verknüpfung des `release` Build-Types mit dieser Signatur.

### 3. Git-Schutz
#### [MODIFY] [.gitignore](file:///home/intux/AndroidStudioProjects/Plattendruck/.gitignore)
- Sicherstellen, dass die `keystore.properties` Datei **nicht** in Git hochgeladen wird, damit deine Passwörter privat bleiben.

## Verification Plan

### Manual Verification
- Nach der Konfiguration führen wir einen Test-Build durch (`./gradlew assembleRelease`).
- Wenn die Passwörter korrekt sind, wird eine signierte APK/AAB erzeugt, die du direkt in den Play Store hochladen kannst.

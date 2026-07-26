# Einbindung des benutzerdefinierten App-Icons

In diesem Schritt wird das von dir bereitgestellte Bild `Plattendruck 2.0.png` als neues offizielles App-Icon integriert.

## User Review Required

> [!IMPORTANT]
> **Adaptive Icon Format:** Ich werde dein Bild als Vordergrund-Ebene für das Android Adaptive Icon verwenden. Da moderne Handys die Form des Icons (rund, quadratisch, etc.) selbst bestimmen, wird das Bild in einen weißen Container eingebettet, um eine perfekte Darstellung auf allen Geräten zu garantieren.
> **Pfad:** Ich kopiere das Bild direkt von deinem Schreibtisch in die App-Ressourcen.

## Proposed Changes

### 1. Bild-Import
- Kopieren der Datei `/home/intux/Schreibtisch/Plattendruck 2.0.png` in den Projektordner `app/src/main/res/drawable/` unter dem Namen `ic_launcher_custom.png`.

### 2. Icon-Konfiguration
#### [MODIFY] [ic_launcher.xml](file:///home/intux/AndroidStudioProjects/Plattendruck/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)
- Umstellung des `foreground`-Attributs auf das neue Bild `@drawable/ic_launcher_custom`.

#### [MODIFY] [ic_launcher_round.xml](file:///home/intux/AndroidStudioProjects/Plattendruck/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml)
- Umstellung des `foreground`-Attributs auf das neue Bild `@drawable/ic_launcher_custom`.

## Verification Plan

### Manual Verification
- Deployment der App auf den Emulator.
- Sichtprüfung des Icons auf dem Home-Screen und in der App-Liste.
- Test, ob das Icon auch in der Task-Übersicht korrekt erscheint.

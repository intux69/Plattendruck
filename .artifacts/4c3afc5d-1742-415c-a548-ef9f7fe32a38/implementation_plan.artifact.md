# Migrierung der Plattendruck-Berechnung in die App (Native Implementation)

Dieses Projekt sieht die Umstellung der App von einem reinen WebView-Wrapper (der eine externe PHP-Seite nutzt) auf eine vollständig native Android-Anwendung vor. Die Berechnungslogik für den statischen Lastplattenversuch nach **DIN 18134** wird direkt in der App implementiert.

## User Review Required

> [!IMPORTANT]
> **Umstellung auf Kotlin:** Ich empfehle, die neue Logik und UI in Kotlin zu schreiben. Das Projekt nutzt aktuell Java, aber Kotlin ist der heutige Standard für Android und bietet bessere Möglichkeiten für mathematische Berechnungen und moderne UI-Komponenten.
> **Benutzeroberfläche:** Soll die App weiterhin wie ein einfaches Formular aussehen, oder sollen wir direkt eine Tabellenansicht mit Diagramm-Vorschau der Kurven planen?

## Proposed Changes

### 1. Mathematische Logik (Calculation Engine)
Implementierung der Regression nach der Methode der kleinsten Quadrate für das Polynom 2. Grades ($s = a_0 + a_1\sigma + a_2\sigma^2$).
- Berechnung von $E_{v1}$ (Erstbelastung)
- Berechnung von $E_{v2}$ (Zweitbelastung)
- Berechnung des Verhältnisses $E_{v2} / E_{v1}$

### 2. Datenmodell
- `TestSession`: Speichert Metadaten (Datum, Ort, Durchmesser) und die Messreihen.
- `MeasurementPoint`: Einzelner Datenpunkt (Spannung $\sigma$, Setzung $s$).

### 3. Benutzeroberfläche (Native UI)
#### [MODIFY] [MainActivity.java](file:///home/intux/AndroidStudioProjects/Plattendruck/app/src/main/java/com/intux/plattendruck/MainActivity.java)
- Umbau zur Startseite, die entweder eine neue Berechnung startet oder die Ergebnisse anzeigt.
- Eventuelle Umbenennung oder Ersetzung durch eine Kotlin-Klasse `MainActivity.kt`.

#### [NEW] `CalculatorViewModel.kt`
- Hält den Zustand der aktuellen Berechnung und führt die Logik aus.

#### [NEW] `layout/activity_calculator.xml`
- Eingabemaske für die 15-18 Messwerte (Erst-, Ent- und Zweitbelastung).

### 4. Abhängigkeiten
#### [MODIFY] [build.gradle](file:///home/intux/AndroidStudioProjects/Plattendruck/app/build.gradle)
- Hinzufügen von Kotlin-Support.
- (Optional) Hinzufügen einer Bibliothek für Matrix-Berechnungen (z.B. Apache Commons Math) oder Implementierung einer einfachen 3x3 Matrix-Inversion.
- (Optional) Hinzufügen einer Chart-Bibliothek (z.B. MPAndroidChart) für die grafische Darstellung der Kurven.

## Verification Plan

### Automated Tests
- Unit Tests für die Berechnungslogik mit bekannten Testwerten aus der DIN 18134, um die Korrektheit der $E_v$-Werte sicherzustellen.

### Manual Verification
- Abgleich der App-Ergebnisse mit den Ergebnissen der aktuellen Website (`plattendruck.org`).
- Prüfung der UI auf verschiedenen Bildschirmgrößen.

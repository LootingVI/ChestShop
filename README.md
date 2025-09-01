# ChestShop Plugin für Paper 1.21.5

**Von Flori entwickelt** - Ein benutzerfreundliches ChestShop Plugin für Paper 1.21.5 mit umfangreichen Konfigurationsmöglichkeiten und professioneller Nachrichtenverwaltung.

## Features

- ✅ **Einfache Shop-Erstellung** - Erstelle Shops mit einem einfachen Befehl
- ✅ **Vault Integration** - Vollständige Economy-Unterstützung
- ✅ **Flexible Konfiguration** - Alles über YAML-Dateien konfigurierbar
- ✅ **Shop-Schutz** - Schutz vor Griefing und unbefugtem Zugriff
- ✅ **Hopper-Protection** - Verhindert Item-Diebstahl über Hopper
- ✅ **Multi-World Support** - Funktioniert in mehreren Welten
- ✅ **Admin-Tools** - Umfangreiche Admin-Befehle mit Statistiken
- ✅ **Detailliertes Logging** - Transaktions- und Shop-Logging
- ✅ **Deutsche Lokalisierung** - Vollständig auf Deutsch
- ✅ **Professionelle Nachrichten** - Standardisierte Nachrichtenverwaltung über messages.yml
- ✅ **Shop-Statistiken** - Detaillierte Analyse von Shop-Performance und Bestand

## Installation

1. **Abhängigkeiten installieren:**
   - Paper 1.21.5+ Server
   - Vault Plugin
   - Ein Economy Plugin (z.B. EssentialsX)

2. **Plugin kompilieren:**
   ```bash
   mvn clean package
   ```

3. **Plugin installieren:**
   - Die generierte `.jar`-Datei aus `target/` in den `plugins/`-Ordner kopieren
   - Server neustarten

## Befehle

### Basis-Befehle
- `/chestshop create <item> <amount> <kaufpreis> [verkaufspreis]` - Shop erstellen
- `/chestshop remove` - Shop entfernen (schaue auf Chest oder Schild)
- `/chestshop info` - Shop-Informationen anzeigen
- `/chestshop list [spieler]` - Shops auflisten
- `/chestshop toggle` - Shop aktivieren/deaktivieren
- `/chestshop help` - Hilfe anzeigen

### Admin-Befehle
- `/chestshop reload` - Konfiguration neu laden

### Aliases
Das Plugin unterstützt folgende Command-Aliases:
- `/cs` (Kurzform)
- `/shop`
- `/cshop`

## Shop-Erstellung

1. **Truhe platzieren** - Platziere eine Truhe an der gewünschten Position
2. **Schild platzieren** - Platziere ein Schild neben, über oder unter der Truhe
3. **Shop erstellen** - Schaue auf die Truhe und führe den Erstellungsbefehl aus:
   ```
   /chestshop create DIAMOND 1 100 90
   ```
   Dies erstellt einen Shop für 1 Diamant mit Kaufpreis 100 und Verkaufspreis 90

## Shop-Nutzung

### Für Kunden:
- **Linksklick** auf Truhe oder Schild = Items kaufen
- **Rechtsklick** auf Truhe oder Schild = Items verkaufen
- **Shift + Klick** = Shop-Informationen anzeigen

### Für Shop-Besitzer:
- Normale Klicks öffnen die Truhe (wenn `owner-free-access` aktiviert ist)
- `/chestshop toggle` zum Aktivieren/Deaktivieren
- `/chestshop remove` zum Löschen

## Konfiguration

### config.yml
Hauptkonfigurationsdatei mit allen Einstellungen:

```yaml
general:
  max-shops-per-player: 10  # Max Shops pro Spieler
  allowed-worlds: []        # Erlaubte Welten (leer = alle)
  auto-save-interval: 5     # Auto-Save in Minuten

shop:
  price-limits:
    min-buy-price: 0.01
    max-buy-price: 1000000.0
  creation:
    creation-cost: 100.0    # Kosten für Shop-Erstellung
    banned-items:           # Verbotene Items
      - "BEDROCK"
      - "COMMAND_BLOCK"

protection:
  enabled: true             # Shop-Schutz aktivieren
  only-owner-break: true    # Nur Owner kann abbauen
  hopper-protection: true   # Hopper-Schutz
```

### messages.yml
Alle Nachrichten sind vollständig anpassbar und auf Deutsch lokalisiert.

### shops.yml
Automatisch generierte Datei mit allen Shop-Daten.

## Permissions

### Standard-Permissions (für alle Spieler):
- `chestshop.use` - Grundlegende Nutzung
- `chestshop.create` - Shops erstellen
- `chestshop.remove` - Eigene Shops entfernen
- `chestshop.info` - Shop-Infos anzeigen
- `chestshop.list` - Eigene Shops auflisten
- `chestshop.toggle` - Eigene Shops aktivieren/deaktivieren

### Admin-Permissions:
- `chestshop.admin` - Alle Admin-Rechte
- `chestshop.reload` - Konfiguration neu laden
- `chestshop.remove.others` - Fremde Shops entfernen
- `chestshop.list.others` - Fremde Shops auflisten
- `chestshop.bypass.*` - Verschiedene Bypass-Rechte

## Beispiele

### Einfacher Verkaufs-Shop:
```bash
/chestshop create STONE 64 0 10
```
Verkauft 64 Stein für 10 (nur Verkauf, kein Kauf)

### Kauf- und Verkaufs-Shop:
```bash
/chestshop create DIAMOND 1 120 100
```
Kauft Diamanten für 120, verkauft für 100

### Nur-Kauf-Shop:
```bash
/chestshop create BREAD 16 5 0
```
Verkauft 16 Brot für 5 (nur Verkauf an Spieler)

## Technische Details

- **Thread-Safe**: Alle Shop-Operationen sind thread-sicher
- **Async-Friendly**: Unterstützt asynchrone Operationen
- **Performance-Optimiert**: Effiziente Datenstrukturen und Caching
- **Memory-Efficient**: Minimaler RAM-Verbrauch
- **Auto-Save**: Automatisches Speichern verhindert Datenverlust

## Support & Entwicklung

Dieses Plugin wurde speziell für Paper 1.21.5 entwickelt und nutzt moderne Bukkit/Paper APIs.

### Entwickler-Informationen:
- Java 21+
- Maven Build-System
- Paper API 1.21.5
- Vault Economy API

## Lizenz

**ChestShop Plugin License** - Dieses Plugin kann frei modifiziert und verwendet werden, jedoch muss die Attribution zum ursprünglichen Autor (Flori) beibehalten werden. Siehe LICENSE Datei für vollständige Details.

**Wichtig für Modifikationen:** Bei jeder Änderung oder Weiterverteilung muss deutlich angegeben werden:
- "Original ChestShop Plugin von Flori"
- Welche Änderungen vorgenommen wurden

## Danksagungen

**Original entwickelt von Flori** - Wenn du dieses Plugin modifizierst oder verwendest, vergiss nicht die Attribution zum ursprünglichen Autor!
# ChestShop
# ChestShop

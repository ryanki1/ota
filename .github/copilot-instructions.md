# Projektregeln für GitHub Copilot – OTA Pipeline Lab

Diese Datei gehört nach `.github/copilot-instructions.md`. Copilot liest sie bei jeder Anfrage in diesem Repo mit.

## Worum es geht

OTA Pipeline Lab ist ein **Lernprojekt**. Es bildet eine Over-the-Air-Update-Pipeline für Fahrzeugsoftware im Kleinen nach:
Fahrzeugfunktion definieren → testbares Softwarepaket konfigurieren → Paket freigeben → in Wellen an Fahrzeuge ausrollen.

Das Projekt ist bewusst fiktiv. Verwende **keine** Namen, Logos oder Begriffe realer Hersteller.

Ich nutze das Projekt, um Kotlin, Kafka, Terraform und Vue zu lernen. Deshalb gilt: **Erklären ist genauso wichtig wie Code.**

## Arbeitsweise

- Arbeite immer nur an **einem Schritt** aus `docs/PLAN.md`. Beginne keinen neuen Schritt ohne meine Zustimmung.
- Schlage vor jedem Schritt kurz vor, welche Dateien du anlegst oder änderst. Warte auf mein OK.
- Erkläre Kotlin-Besonderheiten, die ein erfahrener Java-Entwickler nicht kennt, in einem kurzen Kommentar oder in deiner Antwort (z. B. Sealed Classes, Null-Safety, Extension Functions, `data class`, Scope Functions).
- Jeder Schritt endet mit grünen Tests. Ein Schritt ohne Tests ist nicht fertig.
- Kleine Commits mit Conventional Commits, z. B. `feat(package-service): add release rule`.
- Führe **niemals** `terraform apply` oder `terraform destroy` aus. Das mache ich selbst. `terraform fmt`, `validate` und `plan` sind erlaubt.
- Keine Secrets, Passwörter oder Subscription-IDs im Code. Nutze Umgebungsvariablen oder `.env` (in `.gitignore`).

## Stack und Konventionen

### Backend (services/*)
- Kotlin mit aktueller stabiler Spring-Boot-Version, Java 21, Gradle mit Kotlin DSL.
- Paketstruktur pro Service: `com.otalab.<service>.domain`, `.api`, `.persistence`, `.events`.
- Domänenmodell als `data class`, Status als `sealed class` oder `enum class`.
- Konstruktor-Injection, kein Lombok, keine Feld-Injection.
- REST mit Spring Web, Validierung mit Jakarta Validation, API-Doku mit springdoc-openapi.
- Persistenz mit Spring Data JPA und PostgreSQL, Schema über Flyway.
- Tests: JUnit 5, MockK, Testcontainers für PostgreSQL und Kafka.
- Kafka über Spring for Apache Kafka, Ereignisse als JSON.

### Infrastruktur (infra/terraform)
- Provider `azurerm`, Remote State in einem Azure Storage Account.
- Wiederverwendbare Module unter `modules/`, Umgebung unter `envs/dev/`.
- Kleinste sinnvolle Größen (ein Knoten, günstigste SKUs). Kosten niedrig halten.

### Frontend (frontend/dashboard)
- Vue 3 mit Composition API, Vite, TypeScript, Pinia.
- Erkläre Vue-Konzepte im Vergleich zu Angular, das ich gut kenne (z. B. `ref`/`computed` gegenüber Signals).

### Python (tools/vehicle-simulator)
- Python 3, `requests`, Tests mit pytest.

## Fachbegriffe

| Begriff | Bedeutung |
| --- | --- |
| Feature (Fahrzeugfunktion) | Per Software gelieferte Funktion, braucht bestimmte Steuergeräte |
| SoftwareComponent | Software für ein Steuergerät (ECU) in einer Version (SemVer), mit Prüfsumme und unterstützten Varianten |
| SoftwarePackage | Satz kompatibler Komponenten für eine Fahrzeugvariante. Status: DRAFT → TESTED → RELEASED |
| Vehicle | Simuliertes Fahrzeug: VIN, Variante, installierte Komponentenversionen (Inventar) |
| Campaign | Ausrollen eines freigegebenen Pakets in Wellen (1 % → 10 % → 100 %) |
| AuditEntry | Wer hat wann was geändert, mit Zustand vorher und nachher |

Kernidee: Ein Fahrzeug ist „fällig“, wenn sein gemeldetes Inventar vom freigegebenen Paket seiner Variante abweicht – wie „OutOfSync“ bei ArgoCD.

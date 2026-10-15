# OTA Pipeline Lab – Umsetzungsplan (Mini-Version)

Diese Datei gehört nach `docs/PLAN.md`. Jeder Schritt ist so geschrieben, dass du ihn einzeln an GitHub Copilot im Agent-Modus übergeben kannst.

**Ziel der Mini-Version:** In fünf Abenden bei jeder Lücke der Stellenausschreibung (Kotlin, Kafka, Terraform, Vue, OTA-Domäne) einen ehrlichen Satz aus eigener Praxis sagen können. Kein fertiges System, sondern lauffähige Bausteine.

---

## So arbeitest du mit Copilot

1. `.github/copilot-instructions.md` liegt im Repo. Copilot liest die Projektregeln dann automatisch mit.
2. Öffne pro Schritt einen **neuen** Chat im Agent-Modus und hänge diese Datei an (`#file:docs/PLAN.md`).
3. Kopiere den Prompt des Schritts. Lass dir zuerst den Plan zeigen, dann erst umsetzen.
4. Lies jeden Diff, bevor du ihn annimmst. Frag nach, wenn du eine Zeile nicht erklären könntest – genau das wird im Interview gefragt.
5. Führe die Tests selbst aus. Erst wenn „Fertig, wenn“ erfüllt ist: committen, Haken setzen, nächster Schritt.

Notiere dir nach jedem Schritt in `docs/adr/` oder `docs/notes.md`, wo Copilot geholfen hat und wo du korrigieren musstest. Das ist dein Beispiel für „Agentic Software Engineering“.

## Aktueller Arbeitsstand · 2026-10-15

- Letzter Commit: `b7b1ab4` (`Step 1 complete`).
- Danach geändert, noch nicht committet: `services/package-service/src/main/kotlin/com/otalab/packageservice/PackageController.kt` und neu `services/package-service/src/main/kotlin/com/otalab/packageservice/PackageControllerExceptionHandler.kt` (`@RestControllerAdvice`).
- Verifiziert aus `services/package-service`: `./gradlew clean test` erfolgreich (Exit Code 0).
- Als Nächstes den Diff prüfen. Danach kann Schritt 1 als abgeschlossen behandelt und der nächste Plan-Schritt begonnen werden.

---

## Zielbild: Datenfluss

1. Das Dashboard legt ein Paket an und gibt es frei (Login über Keycloak – kommt erst in der Vollversion).
2. Der `package-service` speichert Paket und Audit-Eintrag in PostgreSQL.
3. Bei der Freigabe sendet er das Ereignis `PackageReleased` an Kafka.
4. Der `rollout-service` konsumiert das Ereignis und legt eine Kampagne an.
5. Er verteilt das Update wellenweise an die simulierten Fahrzeuge.
6. Die Fahrzeuge melden Erfolg oder Fehler zurück. Zu viele Fehler in einer Welle stoppen die Kampagne.
7. Das Dashboard zeigt den Fortschritt pro Welle.

Die Mini-Version deckt die Schritte 1–3 vollständig ab und Schritt 4 als einfachen Consumer.

---

## Repo-Struktur

```
ota-pipeline-lab/
├── .github/
│   ├── copilot-instructions.md
│   └── workflows/              # später: CI
├── services/
│   ├── package-service/        # Kotlin, Spring Boot, Gradle (Kotlin DSL)
│   │   ├── src/main/kotlin/    # domain/, api/, persistence/, events/
│   │   ├── src/main/resources/ # application.yml, db/migration/
│   │   ├── src/test/kotlin/    # JUnit 5, MockK, Testcontainers
│   │   └── Dockerfile
│   └── rollout-service/        # Kotlin, Spring Boot, Kafka-Consumer (Vollversion)
├── tools/
│   └── vehicle-simulator/      # Python, pytest (Vollversion)
├── frontend/
│   └── dashboard/              # Vue 3, Vite, TypeScript, Pinia
├── infra/
│   └── terraform/
│       ├── modules/            # resource-group, acr (später aks, postgres)
│       └── envs/dev/           # main.tf, variables.tf, outputs.tf, backend.tf
├── deploy/
│   ├── helm/                   # Vollversion
│   └── argocd/                 # Vollversion
├── docs/
│   ├── PLAN.md
│   ├── architecture.md
│   └── adr/
├── docker-compose.yml          # PostgreSQL, Kafka
├── .gitignore
└── README.md
```

---

## Schritt 0 · Repo-Gerüst (vor Abend 1, ca. 30 Min.)

**Ziel:** Leeres, sauberes Grundgerüst, das lokal startet.

**Prompt für Copilot:**
> Lege das Repo-Gerüst gemäß Abschnitt „Repo-Struktur“ in docs/PLAN.md an. Erstelle nur Ordner mit `.gitkeep`, eine `.gitignore` für Kotlin/Gradle, Node, Python, Terraform und IDEs, eine kurze `README.md` und eine `docker-compose.yml` mit PostgreSQL 16 und Kafka im KRaft-Modus (ein Broker, ohne Zookeeper). Zeig mir zuerst die geplanten Dateien.

**Fertig, wenn:**
- [ ] `docker compose up -d` startet PostgreSQL und Kafka ohne Fehler
- [ ] `.gitignore` schließt `.env`, `*.tfstate`, `.terraform/`, `build/`, `node_modules/` aus
- [ ] Erster Commit auf `main`

---

## Schritt 1 · package-service mit Kotlin (Abend 1)

**Ziel:** Ein Kotlin-Service, der Softwarepakete anlegt und nach einer fachlichen Regel freigibt.

**Prompt für Copilot:**
> Erstelle in services/package-service einen Spring-Boot-Service in Kotlin (Java 21, Gradle Kotlin DSL) mit den Abhängigkeiten Web, Validation, Actuator und springdoc-openapi. Noch ohne Datenbank: speichere in einem In-Memory-Repository.
> Domäne: `SoftwareComponent` (ecu, version, checksum, supportedVariants) und `SoftwarePackage` (id, variant, components, status). Status als sealed class oder enum: DRAFT, TESTED, RELEASED.
> REST: `POST /packages`, `GET /packages/{id}`, `POST /packages/{id}/test` (DRAFT → TESTED), `POST /packages/{id}/release` (TESTED → RELEASED).
> Regel: Ein Paket darf nur freigegeben werden, wenn es TESTED ist und jede Komponente die Variante des Pakets unterstützt. Sonst HTTP 409 mit verständlicher Fehlermeldung.
> Schreibe Unit-Tests mit JUnit 5 und MockK für die Regel und einen Web-Test für die Endpunkte. Erkläre mir die Kotlin-Besonderheiten, die du verwendest.

**Fertig, wenn:**
- [ ] `./gradlew test` ist grün
- [ ] Swagger UI zeigt alle Endpunkte
- [ ] Ein Paket mit inkompatibler Komponente wird mit 409 abgelehnt
- [ ] Du kannst erklären: `data class`, Null-Safety (`?`, `?.`, `!!`), `sealed class` und `when` als Ausdruck

**Interview-Satz:** „Kotlin habe ich mir in einem eigenen Projekt erarbeitet. Am meisten bringen mir Null-Safety und Data Classes; Spring Boot funktioniert wie in Java.“

---

## Schritt 2 · Persistenz mit PostgreSQL (Abend 1, wenn Zeit bleibt)

**Ziel:** Pakete überleben einen Neustart, Tests laufen gegen eine echte Datenbank.

**Prompt für Copilot:**
> Ersetze im package-service das In-Memory-Repository durch Spring Data JPA mit PostgreSQL aus docker-compose.yml. Lege das Schema mit Flyway an (`V1__init.sql`). Ergänze eine Tabelle `audit_entry` (id, entity_id, action, old_status, new_status, timestamp) und schreibe bei jeder Statusänderung einen Eintrag. Schreibe einen Integrationstest mit Testcontainers. Erkläre mir, worauf man bei JPA-Entitäten in Kotlin achten muss (open classes, No-Arg-Konstruktor, Plugins).

**Fertig, wenn:**
- [ ] Integrationstest mit Testcontainers ist grün
- [ ] Nach einer Freigabe stehen zwei Audit-Einträge in der Datenbank (→ TESTED, → RELEASED)

---

## Schritt 3 · Kafka-Ereignis bei Freigabe (Abend 2)

**Ziel:** Die Freigabe eines Pakets wird als Ereignis veröffentlicht und verarbeitet.

**Prompt für Copilot:**
> Ergänze im package-service Spring for Apache Kafka. Bei einer erfolgreichen Freigabe soll ein Ereignis `PackageReleased` (packageId, variant, components mit ecu und version, releasedAt) als JSON auf das Topic `package-released` gesendet werden. Key ist die Variante.
> Lege zusätzlich in derselben Anwendung einen einfachen `@KafkaListener` an, der das Ereignis empfängt und loggt – als Vorläufer des späteren rollout-service.
> Schreibe einen Integrationstest mit Testcontainers für Kafka, der prüft, dass nach `POST /release` genau ein Ereignis ankommt.
> Erkläre mir dabei Topic, Partition, Key, Consumer Group und Offset an genau diesem Beispiel.

**Fertig, wenn:**
- [ ] Freigabe per REST erzeugt einen Logeintrag im Listener
- [ ] Integrationstest für Kafka ist grün
- [ ] `docs/adr/0001-kafka-statt-rabbitmq.md` geschrieben – **von dir selbst**, nicht von Copilot

**Inhalt des ADR (deine Worte, Stichpunkte):**
- Kontext: Mehrere Dienste sollen auf eine Freigabe reagieren (Rollout, Audit, Benachrichtigung).
- Entscheidung: Kafka als Ereignis-Log.
- Vergleich mit RabbitMQ aus TGW: Log statt Queue, Ereignisse bleiben erhalten und lassen sich erneut abspielen, mehrere Consumer Groups lesen unabhängig.
- Konsequenz: Consumer müssen idempotent sein (ein Ereignis kann mehrfach ankommen).
- Wichtig: Kafka transportiert die **Nachricht über** ein Release, nicht das Softwarepaket selbst. Pakete liegen in einem Speicher und werden von dort geladen.

**Interview-Satz:** „Bei TGW habe ich RabbitMQ produktiv genutzt. Kafka habe ich mir im Lernprojekt erarbeitet; der Hauptunterschied ist Log statt Queue.“

---

## Schritt 4 · Terraform auf Azure (Abend 3)

**Ziel:** Infrastruktur als Code: Ressourcengruppe und Container Registry, reproduzierbar auf- und abbaubar.

**Vorab, von Hand (nicht Copilot):**
- Azure CLI installieren, `az login`
- Budget-Alarm im Azure-Portal setzen
- Storage Account für den Remote State anlegen (Copilot kann dir die `az`-Befehle vorschlagen, ausführen tust du sie)

**Prompt für Copilot:**
> Lege unter infra/terraform eine Terraform-Konfiguration für Azure an: Provider azurerm, Remote State im Azure Storage Account (Werte über `backend.hcl`, nicht im Code), ein Modul `resource-group` und ein Modul `acr` (Container Registry, SKU Basic). Die Umgebung `envs/dev` nutzt beide Module, Variablen für Region und Namenspräfix, Outputs für den ACR-Login-Server.
> Führe nur `terraform fmt`, `terraform validate` und `terraform plan` aus, niemals `apply` oder `destroy`.
> Erkläre mir anhand des Plans: State, Provider, Module, Variablen, Outputs und was „Drift“ bedeutet.

**Danach von Hand:**
- [ ] `terraform apply` – Ressourcen im Portal ansehen
- [ ] Eine Kleinigkeit im Portal ändern, `terraform plan` zeigt die Drift
- [ ] `terraform destroy` – alles wieder weg

**Fertig, wenn:**
- [ ] `apply` und `destroy` laufen sauber durch
- [ ] Du kannst erklären, warum der State remote liegt und gesperrt wird

**Interview-Satz:** „Terraform setze ich bisher im Lernprojekt ein: Module, Remote State, plan/apply. Produktiv habe ich Pipelines mit Azure DevOps und GitOps mit ArgoCD betrieben.“

---

## Schritt 5 · Vue-Dashboard (Abend 4, ca. 1 Stunde)

**Ziel:** Ein Gefühl für Vue 3 im Vergleich zu Angular.

**Prompt für Copilot:**
> Erstelle in frontend/dashboard eine Vue-3-Anwendung mit Vite, TypeScript und Pinia. Eine Seite zeigt die Pakete aus dem package-service (`GET /packages`) als Liste mit Status, eine Schaltfläche ruft `POST /packages/{id}/release` auf und aktualisiert die Liste. Konfiguriere den Vite-Proxy auf den Service.
> Ich kenne Angular sehr gut: Erkläre mir zu jedem Vue-Konzept, das du benutzt, die Entsprechung in Angular (z. B. `ref`/`computed` gegenüber Signals, Pinia gegenüber Service oder NgRx, `<script setup>` gegenüber Komponentenklasse).

**Fertig, wenn:**
- [ ] Liste lädt, Freigabe funktioniert aus der Oberfläche
- [ ] Du kannst drei Unterschiede zwischen Vue und Angular nennen

---

## Abend 4 und 5 · ohne Copilot

Diese Teile sind Lesen und Üben, keine Code-Schritte:

- [ ] Grundbegriffe Software-defined Vehicle, Over-the-Air-Update, Wellen-Rollout (Staged Rollout)
- [ ] UN-Regelungen R155 (Cybersecurity) und R156 (Software-Update-Management) in Grundzügen
- [ ] Unterschied Basis-Update (alle kompatiblen Fahrzeuge) und Funktions-Update (nur mit gültiger Berechtigung)
- [ ] Warum die VIN nur der Schlüssel ist und das gemeldete Inventar über „fällig“ entscheidet
- [ ] Vergleich mit ArgoCD: Soll-Zustand, Ist-Zustand, OutOfSync, Sync in Wellen
- [ ] Spickzettel aus dem Lernplan laut durchsprechen
- [ ] Drei Geschichten (TGW, MBTI Cockpit, Scientific Games) in je zwei Minuten
- [ ] Projekt in fünf Minuten zeigen: Swagger, Kafka-Log, `terraform plan`, Vue-Liste

---

## Danach: Vollversion

Wenn die Stelle kommt oder Zeit bleibt, geht es mit dem Wochenplan aus dem Lernplan weiter: eigener rollout-service mit Kampagnen und Wellen, vehicle-simulator in Python, AKS, CI/CD mit ArgoCD, Keycloak, Paketspeicher und Observability.

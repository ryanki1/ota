# OTA Pipeline Lab

A fictional learning project that models an over-the-air software update pipeline for vehicles.

## Local infrastructure

Start PostgreSQL 16 and a single Kafka broker in KRaft mode:

```sh
docker compose up -d
```

Check service status with `docker compose ps` and stop the services with `docker compose down`.
PostgreSQL data is kept in a named Docker volume when the containers are stopped.

See [docs/PLAN.md](docs/PLAN.md) for the implementation steps and
[.github/copilot-instructions.md](.github/copilot-instructions.md) for project conventions.
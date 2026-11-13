# Neo4flix

Movie recommendation engine: Neo4j (with Graph Data Science), Spring Boot microservices, Angular. Local only, Docker Compose.

## Start

```bash
./scripts/setup.sh          # once: creates .env, JWT keys in secrets/, mkcert certificate in certs/, downloads the GDS jar into plugins/
docker compose up --build
```

Neo4j Browser: http://localhost:7474 (user `neo4j`, password in `.env`).

## Demo accounts (local demo only)

The seed creates 12 users with the same password: `Neo4flix-Demo-2026!`

| Username | Role | Taste |
|---|---|---|
| admin | USER, ADMIN | mixed |
| neo, trinity, ripley, deckard | USER | science fiction |
| andy, red, vito, forrest | USER | drama |
| woody, nemo | USER | animation |
| newbie | USER | no ratings (cold start) |

The BCrypt hash of this password is in `seed/seed.cypher`. It is demo data for a local graph and must not be reused anywhere.

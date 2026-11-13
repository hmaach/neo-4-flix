# Neo4flix (Zone01 Java track)

Solo project, local only, never deployed. Starts with `./scripts/setup.sh && docker compose up --build`.
My priority is the Buy platform. Neo4flix is the minimum that passes every audit line and works.

## References (read when relevant)
- Subject: docs/subject.md
- Audit (acceptance tests, wins over the subject): docs/audit.md
- Decisions: docs/decisions.md
- Audit to feature map: docs/audit-checklist.md

## Scope rule
- Build only what the subject or the audit asks for. A feature with no audit line does not get built.
- Out of scope: Kafka, Redis, Spring Cloud Config, CI, Kubernetes, cloud, refresh tokens, email, social login, admin UI, hexagonal layers, Testcontainers.
- Before coding a feature, name the audit line it satisfies.
- If something seems to need more than this file allows, stop and ask.

## Architecture
- One Neo4j 5 Community container with the Graph Data Science (GDS) plugin. One graph shared by all services: Community has a single user database, and recommendations need the whole graph. Each service writes only the nodes and relationships it owns.
- discovery-server (8761): Eureka server. Every service registers; the gateway and rating-service resolve services by name through it.
- api-gateway (8080): Spring Cloud Gateway (server webflux), routes /api/* to lb://<service>. Routing only: no JWT logic (services verify tokens themselves), no rate limiter (its limiter needs Redis; nginx rate limits instead).
- Four business services, Spring Boot, plain layers (controller, service, repository), Spring Data Neo4j, Eureka client:
  - user-service (8081): register, login, 2FA TOTP, JWT issuing, profile, watchlist, reads own rating history.
  - movie-service (8082): movie CRUD (writes need ADMIN), search, details with average rating, similar movies.
  - rating-service (8083): CRUD on the RATED relationship. Calls movie-service over REST (http://movie-service through Eureka, @LoadBalanced RestClient) to check the movie exists. This is the only call between business services.
  - recommendation-service (8084): GDS node similarity on User-RATED-Movie, filters by genre and year.
- backend/common: shared security config (JWT verification with the RS256 public key), global error handler. No business code.
- frontend: Angular 20 standalone + Material, built into an nginx-unprivileged image. nginx terminates HTTPS, serves the app, rate limits, and proxies /api/* to api-gateway.
- Request path: browser -> nginx (TLS, rate limit) -> api-gateway -> Eureka lookup -> service -> Neo4j.
- Only nginx is published (443, 80 redirects). Eureka dashboard 8761 and Neo4j 7474/7687 are bound to 127.0.0.1 for the audit demo.

## Graph model
- (:User {id, username, email, passwordHash, totpSecretEnc, totpEnabled, roles, createdAt})
- (:Movie {id, title, releaseDate, overview})-[:IN_GENRE]->(:Genre {name})
- (:User)-[:RATED {score: 1..5, ratedAt}]->(:Movie)
- (:User)-[:WATCHLISTED {addedAt}]->(:Movie)
- Unique constraints: User.id, User.username, User.email, Movie.id, Genre.name.
- Ids are app-generated UUID strings, never Neo4j internal ids.

## Stack
- Java 21, Spring Boot 3.5.x, Maven multi-module. Versions only in backend/pom.xml.
- Spring Cloud 2025.0.x: spring-cloud-starter-gateway-server-webflux, spring-cloud-starter-netflix-eureka-server and -client.
- Spring Data Neo4j (bundled with Boot). No Neo4j-OGM dependency: SDN 6+ is its successor.
- JWT: spring-boot-starter-oauth2-resource-server (Nimbus). user-service signs with the private key, the others verify with the public key. Do not copy Buy's common-security.
- TOTP: dev.samstevens.totp.
- Neo4j image pinned (5.26.x community), GDS loaded via NEO4J_PLUGINS.
- Angular 20, Angular Material 20.

## Commands
- Setup once: ./scripts/setup.sh (creates .env, RSA keys in secrets/, mkcert certs in certs/)
- Run: docker compose up --build
- Backend: cd backend && ./mvnw -B clean verify
- Frontend: cd frontend && npm ci && npx ng test --watch=false --browsers=ChromeHeadless && npx ng build

## Code style
I explain every line to auditors. Write code a junior reads on first pass.
- Simplest working solution. No abstraction without a second use.
- Small classes and methods, clear names, standard Spring and Angular patterns only.
- Compose, Dockerfiles, pom, nginx.conf stay flat and explicit.
- If a simple and an enterprise version both meet the audit, pick the simple one and mention the other in one line.
- When you add something non-obvious, tell me in chat why.
- Comments only for flow or why. No Javadoc boilerplate.

## Security rules
- Every Cypher query is parameterized. Never concatenate user input into a query.
- Passwords: BCrypt. Policy enforced server side (Bean Validation) and mirrored in Angular.
- TOTP secrets encrypted at rest (AES-GCM, key from env), because the graph is shown live in Neo4j Browser.
- Every protected endpoint requires ROLE_USER, so a 2FA pending token (no roles) is useless on the API.
- No stack traces or internal messages in API responses.
- Secrets only in .env, secrets/ and certs/ (all gitignored). Never read, print or commit them.
- Containers run as non-root. Pin every image tag, no :latest.

## Tests
- Unit tests for security logic (password policy, TOTP, JWT, crypto) and service classes with mocked repositories. No live infra in unit tests.

## Git
- main plus short-lived feat/* branches merged by PR.
- Commit only when I ask, with my git identity. One commit per concern, each one building.
- Messages: conventional prefix, short lowercase sentence, no period.
- Never add Co-Authored-By or "Generated with" lines. Never push.
- No em dash character. No emojis.

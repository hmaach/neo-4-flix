# Neo4flix setup kit

Kit contents (copy into the repo as shown in step 1):
- CLAUDE.md (repo root)
- docs/subject.md, docs/audit.md, docs/decisions.md, docs/audit-checklist.md

## Big picture

```
Browser --HTTPS (mkcert cert)--> nginx (Angular + TLS + rate limit)
                                   |-- /api/* --> api-gateway (Spring Cloud Gateway)
                                                    |-- /api/auth, /api/users  -> lb://user-service
                                                    |-- /api/movies            -> lb://movie-service
                                                    |-- /api/ratings           -> lb://rating-service -> REST -> lb://movie-service
                                                    |-- /api/recommendations   -> lb://recommendation-service
                                 discovery-server (Eureka): every service registers, lb:// names resolve through it
                                 all four business services -> one Neo4j (with GDS plugin)
```

9 containers: neo4j, neo4j-seed (one shot), discovery-server, api-gateway, 4 business services, frontend (nginx). About 4.5 GB RAM.

What was cut on purpose, and why it still passes: no Kafka (no async requirement), no hexagonal layers, no admin UI (movie writes via Postman with an admin token), no in-app friends (share by link). Each cut is in docs/decisions.md with its fallback.

Audit traps already handled in the design:
1. F10 "Neo4j OGM": Spring Data Neo4j 7 replaced OGM. Do not add OGM, explain D5.
2. F11 GDS: Neo4j container loads the GDS plugin; recommendations use weighted Node Similarity.
3. S4 "valid SSL certificate": mkcert local CA. Valid on any machine where setup.sh ran. Run the audit on your laptop. Fallback in D10.
4. S2 2FA pending token must not work on the API: it carries no roles, every endpoint requires ROLE_USER.
5. TOTP secret visible in Neo4j Browser during the graph demo: encrypted at rest.
6. T2 stress: nginx returns 429 instead of the JVMs falling over.
7. SDN aggregate save can silently delete relationships it did not load: relationship writes go through @Query MERGE/DELETE.

---

## 1. Create the repo

Before starting, ask Zone01 staff whether the audit must clone from the Zone01 Gitea. If yes, add it as a second remote later; nothing else changes.

```bash
# 1. Create and clone (private now; make it public or add the auditor before the audit)
gh repo create neo4flix --private \
  --description "Movie recommendation engine with Neo4j, Spring Boot microservices and Angular" \
  --clone
cd neo4flix

# 2. Drop in the kit (adjust the source path to where you downloaded it)
KIT=~/Downloads/neo4flix-kit
mkdir -p docs
cp "$KIT/CLAUDE.md" .
cp "$KIT"/docs/*.md docs/

# 3. .gitignore
cat > .gitignore <<'EOF'
# secrets and generated files (created by scripts/setup.sh)
.env
secrets/
certs/

# backend
backend/**/target/

# frontend
frontend/node_modules/
frontend/dist/
frontend/.angular/

# IDE and OS
.idea/
.vscode/
*.iml
.DS_Store
EOF

# 4. First commit on main, then work on a feature branch
git add .
git commit -m "docs: project rules, subject, audit and decisions"
git push -u origin main
git switch -c feat/scaffold
```

Branch convention: `main` plus short-lived `feat/<scope>` branches (feat/infra, feat/user-service, feat/frontend...) merged by PR. No dev branch: solo project, no integration environment.

Local prerequisites: Docker, mkcert (`brew install mkcert` or `sudo apt install mkcert libnss3-tools`), openssl, Java 21, Node 22 LTS, apache2-utils (for `ab` in the stress demo).

---

## 2. Claude project

Create a project named "Zone01 Java Track: Neo4flix".

Knowledge files: upload `docs/subject.md`, `docs/audit.md`, `docs/decisions.md`, `docs/audit-checklist.md`. Re-upload decisions.md and audit-checklist.md when they change.

Project instructions (paste as is):

```
Context
I am Hamza, a DevSecOps engineer at CFG Bank, completing the Zone01 Oujda Full-Stack Java specialty (RNCP Level 7). Neo4flix is a separate solo project, audited on site in Oujda. My priority is the Buy platform (deadline January 10, 2027). Neo4flix must be the absolute minimum that passes every audit line and works, so I can spend the remaining time on Buy.

Knowledge files
- subject.md: the official subject.
- audit.md: the acceptance criteria, with ids (F1..F21, S1..S4, T1, T2). They win over the subject when they differ.
- decisions.md: the design decisions and their fallbacks. Treat it as the source of truth for what exists. If it conflicts with my message, ask.
- audit-checklist.md: audit line to feature to demo map.

Stack and environment
- Local only, Docker Compose, never deployed. One command start: ./scripts/setup.sh && docker compose up --build.
- Neo4j 5 Community with the GDS plugin, one shared graph.
- Spring Boot 3.5.x, Spring Cloud 2025.0.x, Java 21, Maven multi-module parent POM.
- discovery-server (Eureka), api-gateway (Spring Cloud Gateway, routing only), four business services (user, movie, rating, recommendation) with Spring Data Neo4j and JWT RS256.
- Angular 20 standalone + Angular Material, served by nginx-unprivileged, which also terminates HTTPS (mkcert), rate limits and proxies /api to the gateway.
- Repo: github.com/hmaach/neo4flix

Scope rule
- Build only what the subject or audit asks for. No Kafka, Redis, Config Server, CI, Kubernetes, cloud, refresh tokens, email, admin UI.
- If I ask for something with no audit line, say so and propose the cheapest option.
- Flag gold-plating every time.

How to work with me
- Be concise, structured, direct. Big picture first, then implementation.
- For every feature, state the audit id it satisfies.
- Recommend the simplest approach that passes the audit. If an enterprise version exists, mention it in one line.
- For code: complete, working files with paths. No placeholders. Minimal comments.
- When I paste errors or logs, give the root cause first, then the fix.
- Security: no secrets in code, non-root containers, pinned image tags, parameterized Cypher. Flag security issues even if I did not ask.
- Help me prepare to explain every decision and every Cypher query to an auditor.
- When information is missing, ask instead of assuming.
- Never use the em dash character. No emojis.
```

---

## 3. Claude Code init prompt

Run Claude Code at the repo root on the `feat/scaffold` branch and paste:

```
You are scaffolding Neo4flix in this repository. First read CLAUDE.md, docs/subject.md, docs/audit.md, docs/decisions.md and docs/audit-checklist.md. CLAUDE.md and decisions.md are binding.

Working mode: stepped. Do exactly one step, then stop. At the end of each step reply with:
1. Files created or changed.
2. Audit ids satisfied (from docs/audit-checklist.md).
3. Verification: commands you ran and their result, plus commands I must run myself (anything needing Docker, mkcert or a browser).
4. Non-obvious choices, 2 to 5 lines each, written so I can defend them to an auditor.
5. Then wait for me to say "next". Never start the next step on your own. Do not commit until I say "commit".

Hard rules for every step:
- Minimum scope. If it has no audit id, do not build it. If you think something is missing, ask.
- Pin every version (Maven, Docker images, npm). Check the version exists before using it. No :latest.
- Containers run as non-root. Files mounted into a container must be readable by that container's uid; handle it in scripts/setup.sh and explain.
- Every Cypher query is parameterized. Relationship writes (RATED, WATCHLISTED) go through @Query MERGE/DELETE, never through saving an aggregate that may not have loaded its relationships.
- Never read, print or commit .env, secrets/ or certs/.
- No em dash character, no emojis, in code, docs or chat.

Step 1. Infra and graph
- .env.example with every variable (NEO4J_PASSWORD, TOTP_ENC_KEY, and any other you need), no real values.
- scripts/setup.sh, idempotent: checks docker, mkcert, openssl; creates .env with random values if missing; generates an RSA 2048 key pair in secrets/; runs mkcert -install and issues certs/cert.pem and certs/key.pem for localhost 127.0.0.1 ::1; sets file permissions for the container uids; prints the next command.
- docker-compose.yml with neo4j only for now: pinned neo4j 5.26.x community image, NEO4J_AUTH from .env, NEO4J_PLUGINS '["graph-data-science"]', gds.* procedures unrestricted, small heap and page cache, ports 7474 and 7687 bound to 127.0.0.1, named volume, healthcheck, mem_limit.
- seed/seed.cypher, idempotent (MERGE): unique constraints from CLAUDE.md; 8 genres; about 40 well known movies with release dates and genres; 12 users including 1 ADMIN, with one demo password (BCrypt hash, documented in README as local demo only); ratings built as 2 or 3 clear taste clusters so recommendations are visibly correct; a few WATCHLISTED edges. Use the exact graph model from CLAUDE.md.
- neo4j-seed one-shot service: same neo4j image, runs cypher-shell -f /seed/seed.cypher after neo4j is healthy, restart "no".
- Verify: compose up neo4j and seed, `RETURN gds.version()`, and the F2 query from audit-checklist.md in Neo4j Browser.

Step 2. Backend foundation
- backend/pom.xml: parent and aggregator, spring-boot-starter-parent 3.5.x, Java 21, spring-cloud-dependencies 2025.0.x import, modules common, discovery-server, api-gateway, user-service, movie-service, rating-service, recommendation-service. All versions here only. Maven wrapper.
- discovery-server: Eureka server, does not register itself, dashboard on 8761.
- api-gateway: spring-cloud-starter-gateway-server-webflux + Eureka client, one route per service (lb://<service>), connect and response timeouts, no security logic, no rate limiter (decisions.md D2). If my Buy gateway helps, ask me to paste it rather than inventing differences.
- backend/common: SecurityConfig (stateless, CSRF off with a comment explaining bearer tokens, resource server JWT using spring.security.oauth2.resourceserver.jwt.public-key-location, roles claim mapped to ROLE_*, every non-public path requires ROLE_USER, public paths from a property), GlobalExceptionHandler returning {status, error, message, fieldErrors} and never stack traces, NotFoundException and ConflictException.
- backend/Dockerfile: one multi-stage file with build arg SERVICE, used by every module including discovery-server and api-gateway (Maven build inside Docker so the auditor needs only Docker), JRE 21 runtime, non-root user, MaxRAMPercentage, HEALTHCHECK on /actuator/health.
- Add discovery-server (8761 bound to 127.0.0.1) and api-gateway (internal only, depends_on discovery-server healthy) to docker-compose.yml with mem_limit.
- Verify: Eureka dashboard on http://127.0.0.1:8761 shows API-GATEWAY.

Step 3. user-service (F6, F7, F18, S1, S2, S3)
- User entity (@Node), repository, BCrypt.
- POST /api/auth/register with password policy from decisions.md D9 (Bean Validation, custom annotation if needed).
- POST /api/auth/login: returns {token} or, if 2FA is on, {mfaRequired: true, mfaToken} (JWT, 5 min, no roles). POST /api/auth/login/2fa {mfaToken, code} returns {token}.
- JWT issuing with NimbusJwtEncoder and the private key: sub, username, roles, 1 h expiry.
- GET/PUT/DELETE /api/users/me.
- 2FA: POST /api/users/me/2fa/setup (returns otpauth URI and QR data URI), /enable {code}, /disable {code}. dev.samstevens.totp. Secret encrypted with AES-GCM using TOTP_ENC_KEY.
- Watchlist: GET /api/users/me/watchlist, POST and DELETE /api/users/me/watchlist/{movieId}.
- GET /api/users/me/ratings: reads the user's RATED relationships (F7).
- Eureka client. Add the service to docker-compose.yml (internal port only, mem_limit, depends_on neo4j and discovery-server healthy). Do the same for every business service in steps 4 to 6.

Step 4. movie-service (F4, F5, F14, F15)
- Movie and Genre entities with IN_GENRE.
- GET /api/movies with optional title (case insensitive contains), genre, yearFrom, yearTo, page, size (max 50).
- GET /api/movies/{id}: details with averageRating and ratingCount.
- POST, PUT, DELETE /api/movies (ADMIN only).
- GET /api/movies/{id}/similar: item to item Cypher (users who rated this 4+ also rated these 4+), fallback to same genre.
- GET /api/movies/genres for the search select.

Step 5. rating-service (F8)
- Rating as @RelationshipProperties (score, ratedAt) with @TargetNode Movie, to show the SDN mapping for F10.
- POST /api/ratings {movieId, score 1..5}, PUT /api/ratings/{movieId}, DELETE /api/ratings/{movieId}, GET /api/ratings/me.
- Before writing, check the movie exists by calling http://movie-service/api/movies/{id} with a @LoadBalanced RestClient (resolved through Eureka), forwarding the caller's bearer token. This is the only call between business services; explain it.

Step 6. recommendation-service (F11, F12, F13, F17)
- GET /api/recommendations?genre=&yearFrom=&yearTo=&limit=
- Algorithm from decisions.md D6: project User-RATED{score}-Movie with a unique graph name, run weighted GDS Node Similarity for the current user (use the filtered variant if the installed GDS version has it in production tier, check with gds.list, otherwise filter the stream), pick movies rated 4+ by similar users and not rated by the current user, score by sum(similarity * score), apply filters, drop the projection in finally. Cold start: top rated movies with the same filters.
- Return each movie with its score and the names of the similar users that drove it (helps explain accuracy in the audit).
- Put the Cypher in docs/recommendation.md with a clause by clause explanation and a Mermaid diagram of the flow.

Step 7. Frontend shell and HTTPS entry point (S4, T2)
- Angular 20 standalone app with Angular Material, routing, auth interceptor (bearer token, sessionStorage), auth guard, error snackbar on HTTP errors.
- Generate with `ng new --skip-tests` and set `skipTests: true` for every schematic in angular.json, so no .spec.ts files are created.
- frontend/Dockerfile: Node build stage, pinned nginx-unprivileged runtime.
- frontend/nginx.conf: HTTP to HTTPS redirect, TLS from mounted certs, HSTS and basic security headers (CSP, X-Content-Type-Options, X-Frame-Options, Referrer-Policy), SPA fallback, /api/* proxied to api-gateway:8080, limit_req (api zone and stricter auth zone), limit_conn, limit_req_status 429, small client_max_body_size, proxy timeouts.
- Complete docker-compose.yml: frontend publishes 443 and 80 only, every service has mem_limit, healthcheck, restart unless-stopped.
- Verify: https://localhost shows a valid padlock.

Step 8. Pages (F14 to F21, S2, S3)
- Login (with 2FA code step), Register (live password policy feedback), Home (movie grid, search bar: title, genre select, year range), Movie details (title, release date, genres, average rating, star rating, watchlist toggle, share, similar movies), My ratings (list, edit, delete), Recommendations (genre and year filters, share per item), Watchlist, Profile (edit, enable or disable 2FA with QR).
- Share: Web Share API with copy-link fallback (decisions.md D11).
- Responsive toolbar with a menu on phone width. Loading and empty states.

Step 9. Audit pass (T1, T2, F1)
- Go through docs/audit-checklist.md line by line and report any gap.
- README.md: one command start, demo accounts, Neo4j Browser queries for F2 and F3, curl examples per service (F4, F6, F8 via Postman or curl), a "malicious input" curl list (no token, tampered token, mfa token on the API, user token on admin endpoint, invalid JSON, oversized body, score 0 and 6, invalid UUID, Cypher and HTML injection strings in search and fields), the expected status for each, and a stress section with ab showing 429s and the app still responding.
- Deliverable for T1 and T2: scripts/audit-checks.sh, which runs the malicious input curls and the ab stress test against the running stack and prints expected vs actual status for each line.
```

---

## 4. Audit to feature checklist

See docs/audit-checklist.md (also uploaded to the Claude project). Every row is satisfied by a step above:

| Step | Audit ids |
|---|---|
| 1 Infra and graph | F2, F3, F11 (GDS loaded) |
| 2 Backend foundation, Eureka, gateway | F1, S1, T1 |
| 3 user-service | F6, F7, F18, S1, S2, S3, S4 (hashes, encrypted TOTP) |
| 4 movie-service | F4, F5, F14, F15 |
| 5 rating-service | F8, F10 |
| 6 recommendation-service | F9, F11, F12, F13, F17 |
| 7 Frontend shell and nginx | S4, T2 |
| 8 Pages | F14 to F21, S2, S3 |
| 9 Audit pass | F1, T1, T2 |

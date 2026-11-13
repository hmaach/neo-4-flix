# Decisions

Each decision: what, why, rejected alternative. Use these lines to answer auditors and the RNCP jury.

## D1. Local only, Docker Compose
- What: the whole stack starts with `./scripts/setup.sh && docker compose up --build`. No deployment.
- Why: the audit says "run docker to initiate the web application". Nothing asks for hosting.
- Rejected: VM or cloud hosting (time, no audit value).

## D2. API gateway behind nginx
- What: api-gateway (Spring Cloud Gateway, server webflux) is the single entry to the backend and routes /api/* to lb://<service>. nginx in the frontend container sits in front: it terminates HTTPS, serves Angular, rate limits and proxies /api/* to the gateway.
- Why: a gateway is the expected microservice entry point and the pattern auditors know from buy-01. It keeps service addresses hidden from the client. The gateway does routing only: services verify JWTs themselves (defense in depth, no single point of trust), and rate limiting stays in nginx because the gateway's RequestRateLimiter needs Redis.
- Rejected: nginx routing straight to services (works, but no visible gateway). Redis for gateway rate limiting (one more container for the same result).

## D3. Service discovery with Eureka
- What: discovery-server (Eureka). Every service registers. The gateway routes with lb://movie-service, and rating-service calls http://movie-service with a @LoadBalanced RestClient.
- Why: services are found by name, not by host and port, so an instance can move or be scaled (`docker compose up --scale movie-service=2`) without config changes. Easy to demo: the Eureka dashboard on 127.0.0.1:8761 lists every instance.
- Note: services take 30 to 60 s to register after start. A 503 from the gateway right after startup means "not registered yet".
- Rejected: static Docker DNS names (works with one instance, but no discovery to show).

## D4. One shared Neo4j graph
- What: all services use the same Neo4j database. Each service writes only what it owns (user-service: User and WATCHLISTED, movie-service: Movie and Genre, rating-service: RATED). recommendation-service only reads.
- Why: Neo4j Community has one user database, and graph recommendations need users, movies and ratings in one graph. Splitting it would destroy the value of a graph.
- Rejected: one database per service (impossible in Community, useless for traversal).

## D5. Spring Data Neo4j instead of Neo4j-OGM (audit F10 trap)
- What: SDN 7 (bundled with Boot 3.5) maps classes with @Node, @Relationship, @RelationshipProperties, @TargetNode.
- Why: Neo4j-OGM was the mapping layer of SDN 5. SDN 6+ replaced it with its own object graph mapping and OGM is not supported by Boot 3. Answer for F10: "the OGM role is played by SDN's mapping layer; here is the Rating relationship entity".
- Rejected: adding neo4j-ogm alongside SDN (two mappers, unsupported).

## D6. Recommendations with GDS Node Similarity (collaborative filtering)
- What: recommendation-service projects User-[:RATED {score}]->Movie into GDS, runs weighted Node Similarity to find users with similar taste, then Cypher picks movies those users rated 4+ that the current user has not rated, scored by sum(similarity * score). Filters: genre, release year range. Cold start (no ratings): top rated movies.
- Why: audit F11 asks for an algorithm from the Graph Algorithms Library (now GDS). Node Similarity is the standard collaborative filtering building block.
- Projection: one per request with a unique name, dropped in a finally block. Fine at seed scale (milliseconds). At real scale you would keep a named projection refreshed on a schedule.
- movie-service "similar movies" uses plain Cypher item-to-item filtering (users who liked this also liked), to cover F5.

## D7. JWT RS256, Boot resource server
- What: user-service signs with the RSA private key (NimbusJwtEncoder). Other services verify with the public key via `spring.security.oauth2.resourceserver.jwt.public-key-location`. Claims: sub (user id), username, roles, exp 1 h.
- Why: only the service that owns credentials can mint tokens. Verification is one Boot property, no custom filter.
- Rejected: HS256 shared secret (every service could forge tokens). Refresh tokens (not asked).

## D8. 2FA with TOTP, optional per user
- What: user enables 2FA from the profile page (QR code, confirm with a 6 digit code). Login then returns a 5 minute "mfa pending" token with no roles; POST the code to get the real JWT.
- Why: S2. TOTP works offline with any authenticator app; no SMS or email provider.
- The pending token is a JWT without roles, and every API endpoint requires ROLE_USER, so it cannot be used on the API.
- TOTP secret encrypted at rest (AES-GCM, key in .env), because the graph is shown live in Neo4j Browser.

## D9. Password policy
- What: 12 to 128 characters, at least one upper case, one lower case, one digit, one special character. Bean Validation on the server, same rule as an Angular validator with live feedback. BCrypt hashing.
- Why: S3. The server is the real enforcement; the client rule is only UX.

## D10. Valid certificate with mkcert (audit S4 trap)
- What: scripts/setup.sh runs `mkcert -install` and issues a certificate for localhost, 127.0.0.1 and ::1. The browser shows a valid padlock with no warning.
- Why: Let's Encrypt cannot issue for localhost. mkcert creates a local CA trusted by the machine, which is the standard way to get a valid certificate for local HTTPS.
- Condition: the audit must run on a machine where setup.sh ran (my laptop, or the auditor runs the script). A self-signed certificate would show a warning and likely fail S4.
- Fallback if an auditor insists on a public CA: a duckdns name pointing to 127.0.0.1 with a Let's Encrypt certificate via DNS-01. Not built unless needed.

## D11. Sharing by link
- What: a Share button on movie details and on each recommendation uses the Web Share API (native share sheet on mobile) with a copy-link fallback.
- Why: F19 asks to "easily share with friends". No backend, no friend graph.
- Risk: an auditor may expect in-app sharing to another user. Fallback, if needed: (:User)-[:SHARED_WITH {at}]->(:User) plus a "shared with me" list.

## D12. Graceful under stress (audit T2)
- What: nginx limit_req in front of the gateway (general API zone, stricter zone on /api/auth), limit_conn, small client_max_body_size, proxy timeouts, 429 on excess. Gateway timeouts (connect and response) so a slow service returns 504 instead of hanging. mem_limit on every container. Healthchecks and restart: unless-stopped.
- Why: load demo shows 429s and the app staying responsive instead of crashing.

## D13. Error handling (audit T1)
- What: one @RestControllerAdvice in backend/common returns `{status, error, message, fieldErrors}`. Never stack traces. Validation on every DTO (sizes, ranges, UUID format). Parameterized Cypher only. Angular escapes output by default.

## D14. Plain layered services
- What: controller, service, repository per service.
- Why: four small CRUD services. Hexagonal layers would triple file count with no audit value.

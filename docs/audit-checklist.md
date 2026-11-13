# Audit to feature checklist

Tick a line only after demoing it end to end on a fresh `docker compose down && ./scripts/setup.sh && docker compose up --build` (keep the volume unless you want to test the seed).

| Id | Audit line (short) | Feature that satisfies it | How to demo |
|---|---|---|---|
| F1 | App works, navigable via browser or Postman | Compose stack, nginx on https://localhost, gateway routing, Eureka, README curl examples | Fresh clone, setup.sh, compose up, Eureka dashboard http://127.0.0.1:8761 shows every service, open https://localhost |
| F2 | Nodes represent movies, users, ratings | User, Movie, Genre nodes; RATED, WATCHLISTED, IN_GENRE relationships | Neo4j Browser http://localhost:7474: `MATCH p=(u:User)-[:RATED]->(m:Movie)-[:IN_GENRE]->(g) RETURN p LIMIT 80` |
| F3 | Relationships carry proper attributes | RATED {score, ratedAt}, WATCHLISTED {addedAt} | Click a RATED edge in Neo4j Browser |
| F4 | Movie service reads and writes movies | GET/POST/PUT/DELETE /api/movies (writes ADMIN) | Postman or curl with admin token |
| F5 | Movie service generates recommendations from ratings | GET /api/movies/{id}/similar (users who liked this also liked) | "Similar movies" block on details page, show the Cypher |
| F6 | User service reads and writes user data | register, GET/PUT /api/users/me, DELETE /api/users/me | Register, edit profile |
| F7 | User service stores user ratings | GET /api/users/me/ratings reads the RATED relationships of the user | My ratings page, then the same in Neo4j Browser |
| F8 | Rating service handles rating data | POST/PUT/DELETE /api/ratings, GET /api/ratings/me, score 1..5, ratedAt | Rate, update, delete; watch the edge change in Neo4j Browser |
| F9 | Spring Data Neo4j used | spring-boot-starter-data-neo4j in every service | Show a repository interface |
| F10 | Neo4j OGM mapping | SDN mapping: @Node, @Relationship, @RelationshipProperties (OGM successor, see D5) | Show the Rating relationship entity and explain D5 |
| F11 | Graph algorithm library used | GDS weighted Node Similarity (collaborative filtering) | Show `RETURN gds.version()` and the recommendation query |
| F12 | Auditee understands Cypher | Recommendation and similar-movies queries | Explain each clause; run them by hand in Neo4j Browser |
| F13 | Recommendations accurate | Seed has taste clusters (sci-fi fans, drama fans...) | Log in as a sci-fi seed user, recommendations are sci-fi; rate a drama 5, refresh, list shifts |
| F14 | Search by title, genre, release date | GET /api/movies?title=&genre=&yearFrom=&yearTo= | Search bar with title field, genre select, year range |
| F15 | Details: title, release date, genre, rating | Movie details page with average rating and count | Open a movie |
| F16 | Rating page works | Star rating on details, My ratings page with edit and delete | Rate, change, delete |
| F17 | Recommendations from ratings | Recommendations page with genre and year filters | Open page, change filters |
| F18 | Watchlist | POST/DELETE /api/users/me/watchlist/{movieId}, Watchlist page | Add from details, see it in Watchlist |
| F19 | Share recommendations | Share button (Web Share API, copy link fallback) | Click Share, paste the link in another browser |
| F20 | User-friendly UI | Material toolbar, responsive grid, snackbars for errors, loading states | Resize to phone width |
| F21 | Essential pages work | login, register, home, details, my ratings, recommendations, watchlist, profile | Walk through every page |
| S1 | JWT properly implemented | RS256, user-service signs, others verify with public key, roles claim, 1 h expiry | Call API without token (401), with tampered token (401), user token on admin endpoint (403) |
| S2 | 2FA | TOTP enable on profile with QR, login asks for code | Enable with an authenticator app, log out, log in |
| S3 | Strong password policy | 12+ chars, upper, lower, digit, special; server and client | Register with "password" (rejected by server even via curl) |
| S4 | HTTPS, valid certificate, sensitive data protected | nginx TLS with mkcert cert, HTTP redirects to HTTPS, HSTS, BCrypt hashes, encrypted TOTP secret | Padlock in browser; show passwordHash and totpSecretEnc in Neo4j Browser |
| T1 | Handles unexpected and malicious input | Validation, global error handler, parameterized Cypher, body size limit | Run the README "malicious input" curl list |
| T2 | Graceful under stress | nginx rate limits (429), mem limits, healthchecks | `ab -n 2000 -c 100 https://localhost/api/movies` then show the app still responds |

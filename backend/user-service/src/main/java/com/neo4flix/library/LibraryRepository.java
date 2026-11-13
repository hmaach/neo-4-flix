package com.neo4flix.library;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

// Read-only queries that return movie rows with relationship data (addedAt, score).
// They are not User entities, so they go through Neo4jClient with explicit, parameterized Cypher.
@Repository
public class LibraryRepository {

    private final Neo4jClient neo4jClient;

    public LibraryRepository(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public List<WatchlistItem> findWatchlist(String userId) {
        String cypher = """
            MATCH (:User {id: $userId})-[w:WATCHLISTED]->(m:Movie)
            OPTIONAL MATCH (m)-[:IN_GENRE]->(g:Genre)
            WITH m, w, collect(g.name) AS genres
            RETURN m.id AS id, m.title AS title, m.releaseDate AS releaseDate, genres, w.addedAt AS addedAt
            ORDER BY w.addedAt DESC
            """;
        return List.copyOf(neo4jClient.query(cypher)
            .bind(userId).to("userId")
            .fetchAs(WatchlistItem.class)
            .mappedBy((typeSystem, row) -> new WatchlistItem(
                row.get("id").asString(),
                row.get("title").asString(),
                toLocalDate(row.get("releaseDate")),
                row.get("genres").asList(Value::asString),
                toInstant(row.get("addedAt"))))
            .all());
    }

    public List<RatedMovie> findRatings(String userId) {
        String cypher = """
            MATCH (:User {id: $userId})-[r:RATED]->(m:Movie)
            OPTIONAL MATCH (m)-[:IN_GENRE]->(g:Genre)
            WITH m, r, collect(g.name) AS genres
            RETURN m.id AS id, m.title AS title, m.releaseDate AS releaseDate, genres,
                   r.score AS score, r.ratedAt AS ratedAt
            ORDER BY r.ratedAt DESC
            """;
        return List.copyOf(neo4jClient.query(cypher)
            .bind(userId).to("userId")
            .fetchAs(RatedMovie.class)
            .mappedBy((typeSystem, row) -> new RatedMovie(
                row.get("id").asString(),
                row.get("title").asString(),
                toLocalDate(row.get("releaseDate")),
                row.get("genres").asList(Value::asString),
                row.get("score").asInt(),
                toInstant(row.get("ratedAt"))))
            .all());
    }

    private static LocalDate toLocalDate(Value value) {
        return value.isNull() ? null : value.asLocalDate();
    }

    private static Instant toInstant(Value value) {
        return value.isNull() ? null : value.asZonedDateTime().toInstant();
    }
}

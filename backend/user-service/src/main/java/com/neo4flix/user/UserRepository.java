package com.neo4flix.user;

import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends Neo4jRepository<User, String> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Returns false when the user or the movie does not exist (MATCH finds no row, count is 0).
    // MERGE makes it idempotent: adding twice keeps one edge and the first addedAt.
    @Transactional
    @Query("""
        MATCH (u:User {id: $userId}), (m:Movie {id: $movieId})
        MERGE (u)-[w:WATCHLISTED]->(m)
        ON CREATE SET w.addedAt = datetime()
        RETURN count(w) > 0
        """)
    boolean addToWatchlist(String userId, String movieId);

    @Transactional
    @Query("""
        MATCH (:User {id: $userId})-[w:WATCHLISTED]->(:Movie {id: $movieId})
        DELETE w
        """)
    void removeFromWatchlist(String userId, String movieId);

    // DETACH also removes the user's RATED and WATCHLISTED edges: a deleted account leaves nothing behind.
    @Transactional
    @Query("MATCH (u:User {id: $userId}) DETACH DELETE u")
    void deleteWithRelationships(String userId);
}

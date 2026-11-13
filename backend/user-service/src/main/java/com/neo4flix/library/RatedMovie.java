package com.neo4flix.library;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record RatedMovie(String movieId, String title, LocalDate releaseDate, List<String> genres, int score,
                         Instant ratedAt) {
}

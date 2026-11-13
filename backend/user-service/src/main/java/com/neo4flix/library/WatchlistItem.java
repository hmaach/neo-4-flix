package com.neo4flix.library;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record WatchlistItem(String movieId, String title, LocalDate releaseDate, List<String> genres, Instant addedAt) {
}

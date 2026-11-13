package com.neo4flix.library;

import com.neo4flix.common.error.NotFoundException;
import com.neo4flix.user.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

// The user's own movies: watchlist (F18) and rating history (F7).
@Service
public class LibraryService {

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;

    public LibraryService(UserRepository userRepository, LibraryRepository libraryRepository) {
        this.userRepository = userRepository;
        this.libraryRepository = libraryRepository;
    }

    public List<WatchlistItem> watchlist(String userId) {
        return libraryRepository.findWatchlist(userId);
    }

    public void addToWatchlist(String userId, String movieId) {
        if (!userRepository.addToWatchlist(userId, movieId)) {
            throw new NotFoundException("Movie not found");
        }
    }

    // Idempotent: removing a movie that is not in the watchlist is not an error.
    public void removeFromWatchlist(String userId, String movieId) {
        userRepository.removeFromWatchlist(userId, movieId);
    }

    public List<RatedMovie> ratings(String userId) {
        return libraryRepository.findRatings(userId);
    }
}

package com.neo4flix.library;

import java.util.List;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// The user id always comes from the token subject, never from the URL: a user can only read their own data.
@RestController
@RequestMapping("/api/users/me")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/watchlist")
    public List<WatchlistItem> watchlist(@AuthenticationPrincipal Jwt jwt) {
        return libraryService.watchlist(jwt.getSubject());
    }

    @PostMapping("/watchlist/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addToWatchlist(@AuthenticationPrincipal Jwt jwt, @PathVariable @UUID String movieId) {
        libraryService.addToWatchlist(jwt.getSubject(), movieId);
    }

    @DeleteMapping("/watchlist/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromWatchlist(@AuthenticationPrincipal Jwt jwt, @PathVariable @UUID String movieId) {
        libraryService.removeFromWatchlist(jwt.getSubject(), movieId);
    }

    @GetMapping("/ratings")
    public List<RatedMovie> ratings(@AuthenticationPrincipal Jwt jwt) {
        return libraryService.ratings(jwt.getSubject());
    }
}

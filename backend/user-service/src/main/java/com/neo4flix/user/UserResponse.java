package com.neo4flix.user;

import java.time.Instant;
import java.util.List;

// What the API shows about a user. Never the password hash or the TOTP secret.
public record UserResponse(String id, String username, String email, List<String> roles, boolean totpEnabled,
                           Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRoles(),
            user.isTotpEnabled(), user.getCreatedAt());
    }
}

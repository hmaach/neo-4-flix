package com.neo4flix.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_.-]{3,30}$", message = "must be 3 to 30 letters, digits, dot, dash or underscore")
    String username,

    @NotBlank @Email @Size(max = 254)
    String email,

    @StrongPassword
    String password) {
}

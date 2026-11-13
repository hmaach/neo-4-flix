package com.neo4flix.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MfaLoginRequest(
    @NotBlank @Size(max = 4096) String mfaToken,
    @NotBlank @Pattern(regexp = "^\\d{6}$", message = "must be 6 digits") String code) {
}

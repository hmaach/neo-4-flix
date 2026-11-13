package com.neo4flix.twofactor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TwoFactorCodeRequest(
    @NotBlank @Pattern(regexp = "^\\d{6}$", message = "must be 6 digits") String code) {
}

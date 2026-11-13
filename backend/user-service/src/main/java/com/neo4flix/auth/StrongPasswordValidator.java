package com.neo4flix.auth;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    static final int MIN_LENGTH = 12;
    // BCrypt only uses the first 72 bytes, and Spring Security refuses longer input instead of truncating it.
    static final int MAX_BYTES = 72;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        return password.length() >= MIN_LENGTH
            && password.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES
            && password.chars().anyMatch(Character::isUpperCase)
            && password.chars().anyMatch(Character::isLowerCase)
            && password.chars().anyMatch(Character::isDigit)
            && password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
    }
}

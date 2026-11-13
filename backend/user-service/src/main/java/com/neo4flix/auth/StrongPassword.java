package com.neo4flix.auth;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Password policy (decisions D9, audit S3). Rules live in StrongPasswordValidator.
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {

    String message() default "must be at least 12 characters (at most 72 bytes) with an upper case letter, "
        + "a lower case letter, a digit and a special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

package com.neo4flix.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

// Either {token} or, when 2FA is on, {mfaRequired: true, mfaToken}. Null fields are left out of the JSON.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(String token, Boolean mfaRequired, String mfaToken) {

    public static LoginResponse withToken(String token) {
        return new LoginResponse(token, null, null);
    }

    public static LoginResponse mfaRequired(String mfaToken) {
        return new LoginResponse(null, true, mfaToken);
    }
}

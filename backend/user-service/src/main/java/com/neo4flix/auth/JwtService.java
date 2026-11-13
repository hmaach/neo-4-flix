package com.neo4flix.auth;

import com.neo4flix.user.User;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JwtService {

    static final Duration ACCESS_TOKEN_TTL = Duration.ofHours(1);
    static final Duration MFA_TOKEN_TTL = Duration.ofMinutes(5);
    static final String MFA_CLAIM = "mfa_pending";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    // The real token: subject is the user id, roles drive authorization in every service.
    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(user.getId())
            .claim("username", user.getUsername())
            .claim("roles", user.getRoles())
            .issuedAt(now)
            .expiresAt(now.plus(ACCESS_TOKEN_TTL))
            .build();
        return encode(claims);
    }

    // Proves "password was correct" for 5 minutes. No roles claim, so every API endpoint rejects it (403).
    public String issueMfaToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(user.getId())
            .claim(MFA_CLAIM, true)
            .issuedAt(now)
            .expiresAt(now.plus(MFA_TOKEN_TTL))
            .build();
        return encode(claims);
    }

    // Checks signature and expiry, and that this is an mfa token, not an access token. Returns the user id.
    public String userIdFromMfaToken(String mfaToken) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(mfaToken);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired 2FA session");
        }
        if (!Boolean.TRUE.equals(jwt.getClaim(MFA_CLAIM))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired 2FA session");
        }
        return jwt.getSubject();
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

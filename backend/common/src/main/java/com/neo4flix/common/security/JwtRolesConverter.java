package com.neo4flix.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

// Turns the "roles" claim ["USER", "ADMIN"] into ROLE_USER, ROLE_ADMIN so hasRole("USER") and @PreAuthorize work.
// A token without a roles claim (the 2FA pending token) gets no authority and is rejected everywhere.
public class JwtRolesConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();

    public JwtRolesConverter() {
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, authorities.convert(jwt));
    }
}

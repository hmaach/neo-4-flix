package com.neo4flix.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

// Signing side of JWT RS256 (D7). The JwtDecoder used to verify tokens is created by Spring Boot
// from spring.security.oauth2.resourceserver.jwt.public-key-location, like in every other service.
@Configuration
public class JwtKeysConfig {

    @Bean
    JwtEncoder jwtEncoder(@Value("${app.jwt.private-key-location}") Resource privateKeyFile,
                          @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}") Resource publicKeyFile)
        throws IOException {
        RSAPrivateKey privateKey;
        try (InputStream in = privateKeyFile.getInputStream()) {
            privateKey = RsaKeyConverters.pkcs8().convert(in);
        }
        RSAPublicKey publicKey;
        try (InputStream in = publicKeyFile.getInputStream()) {
            publicKey = RsaKeyConverters.x509().convert(in);
        }
        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

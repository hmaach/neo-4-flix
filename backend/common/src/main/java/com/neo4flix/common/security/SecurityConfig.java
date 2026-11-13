package com.neo4flix.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo4flix.common.error.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.RequestRejectedHandler;

// Stateless resource server: every request carries a JWT signed by user-service, verified here with the
// RS256 public key (spring.security.oauth2.resourceserver.jwt.public-key-location in each service).
// No CORS config on purpose: nginx serves the app and /api on the same origin, so browsers never send a
// cross-origin request, and any other origin is refused by the browser.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProperties properties,
                                            ObjectMapper objectMapper) throws Exception {
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint(objectMapper);
        JsonAccessDeniedHandler accessDeniedHandler = new JsonAccessDeniedHandler(objectMapper);

        http
            // No session and no cookie: the token travels in the Authorization header, which a cross-site
            // form or image cannot set, so CSRF protection has nothing to protect.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/actuator/health").permitAll();
                if (!properties.publicPaths().isEmpty()) {
                    auth.requestMatchers(properties.publicPaths().toArray(String[]::new)).permitAll();
                }
                auth.anyRequest().hasRole("USER");
            })
            // Same JSON handlers for "no token" (exceptionHandling) and "bad token" (resource server filter).
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .oauth2ResourceServer(oauth2 -> oauth2
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtRolesConverter())));
        return http.build();
    }

    // Spring Security's firewall rejects paths with "..", ";" or encoded dots before any controller, which blocks
    // traversal like /api/users/../../actuator/env. Answer 400 directly: the default sendError(400) re-dispatches
    // to /error without the user's authentication and would surface as a misleading 401.
    @Bean
    RequestRejectedHandler requestRejectedHandler(ObjectMapper objectMapper) {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiError body = new ApiError(400, "Bad Request", "Malformed request path", Map.of());
            objectMapper.writeValue(response.getOutputStream(), body);
        };
    }
}

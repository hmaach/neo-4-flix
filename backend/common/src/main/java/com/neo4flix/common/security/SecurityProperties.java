package com.neo4flix.common.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

// app.security.public-paths: endpoints reachable without a token (register, login). Everything else needs ROLE_USER.
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(List<String> publicPaths) {

    public SecurityProperties {
        publicPaths = publicPaths == null ? List.of() : List.copyOf(publicPaths);
    }
}

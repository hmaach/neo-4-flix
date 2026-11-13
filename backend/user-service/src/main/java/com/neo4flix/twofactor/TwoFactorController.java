package com.neo4flix.twofactor;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/2fa")
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    public TwoFactorController(TwoFactorService twoFactorService) {
        this.twoFactorService = twoFactorService;
    }

    @PostMapping("/setup")
    public TwoFactorSetupResponse setup(@AuthenticationPrincipal Jwt jwt) {
        return twoFactorService.setup(jwt.getSubject());
    }

    @PostMapping("/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TwoFactorCodeRequest request) {
        twoFactorService.enable(jwt.getSubject(), request.code());
    }

    @PostMapping("/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TwoFactorCodeRequest request) {
        twoFactorService.disable(jwt.getSubject(), request.code());
    }
}

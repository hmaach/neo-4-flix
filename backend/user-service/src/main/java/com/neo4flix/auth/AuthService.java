package com.neo4flix.auth;

import com.neo4flix.common.error.ConflictException;
import com.neo4flix.twofactor.SecretCipher;
import com.neo4flix.twofactor.TotpService;
import com.neo4flix.user.User;
import com.neo4flix.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String BAD_CREDENTIALS = "Invalid username or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final SecretCipher secretCipher;
    // Compared against when the username does not exist, so both cases take the same BCrypt time
    // and response timing does not reveal which usernames exist.
    private final String dummyHash;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       TotpService totpService, SecretCipher secretCipher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.totpService = totpService;
        this.secretCipher = secretCipher;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    public User register(RegisterRequest request) {
        String email = request.email().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }
        User user = new User(UUID.randomUUID().toString(), request.username(), email,
            passwordEncoder.encode(request.password()), null, false, List.of("USER"), Instant.now());
        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        Optional<User> found = userRepository.findByUsername(request.username());
        String hash = found.map(User::getPasswordHash).orElse(dummyHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), hash);
        if (found.isEmpty() || !passwordMatches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }
        User user = found.get();
        if (user.isTotpEnabled()) {
            return LoginResponse.mfaRequired(jwtService.issueMfaToken(user));
        }
        return LoginResponse.withToken(jwtService.issueAccessToken(user));
    }

    public LoginResponse loginWithTwoFactor(MfaLoginRequest request) {
        String userId = jwtService.userIdFromMfaToken(request.mfaToken());
        User user = userRepository.findById(userId)
            .filter(User::isTotpEnabled)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired 2FA session"));
        String secret = secretCipher.decrypt(user.getTotpSecretEnc(), user.getId());
        if (!totpService.isValidCode(secret, request.code())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid 2FA code");
        }
        return LoginResponse.withToken(jwtService.issueAccessToken(user));
    }
}

package com.neo4flix.twofactor;

import com.neo4flix.common.error.ConflictException;
import com.neo4flix.common.error.NotFoundException;
import com.neo4flix.user.User;
import com.neo4flix.user.UserRepository;
import dev.samstevens.totp.qr.QrData;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// Flow (D8): setup stores a new encrypted secret and returns the QR code; enable turns 2FA on only after the
// user proves their app produces valid codes; disable needs a valid code too, so a stolen token alone cannot.
@Service
public class TwoFactorService {

    private final UserRepository userRepository;
    private final TotpService totpService;
    private final SecretCipher secretCipher;

    public TwoFactorService(UserRepository userRepository, TotpService totpService, SecretCipher secretCipher) {
        this.userRepository = userRepository;
        this.totpService = totpService;
        this.secretCipher = secretCipher;
    }

    public TwoFactorSetupResponse setup(String userId) {
        User user = findUser(userId);
        if (user.isTotpEnabled()) {
            throw new ConflictException("2FA is already enabled");
        }
        String secret = totpService.newSecret();
        user.setTotpSecretEnc(secretCipher.encrypt(secret, user.getId()));
        userRepository.save(user);
        QrData qrData = totpService.qrData(secret, user.getUsername());
        return new TwoFactorSetupResponse(qrData.getUri(), totpService.qrImageDataUri(qrData));
    }

    public void enable(String userId, String code) {
        User user = findUser(userId);
        if (user.isTotpEnabled()) {
            throw new ConflictException("2FA is already enabled");
        }
        if (user.getTotpSecretEnc() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start 2FA setup first");
        }
        requireValidCode(user, code);
        user.setTotpEnabled(true);
        userRepository.save(user);
    }

    public void disable(String userId, String code) {
        User user = findUser(userId);
        if (!user.isTotpEnabled()) {
            throw new ConflictException("2FA is not enabled");
        }
        requireValidCode(user, code);
        user.setTotpEnabled(false);
        user.setTotpSecretEnc(null);
        userRepository.save(user);
    }

    private void requireValidCode(User user, String code) {
        String secret = secretCipher.decrypt(user.getTotpSecretEnc(), user.getId());
        if (!totpService.isValidCode(secret, code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid 2FA code");
        }
    }

    private User findUser(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    }
}

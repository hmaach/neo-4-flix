package com.neo4flix.twofactor;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.stereotype.Service;

// RFC 6238 TOTP: 6 digits, 30 second period, SHA1 (what Google Authenticator and others expect).
// The verifier accepts the previous and next period too, to absorb clock drift between phone and server.
@Service
public class TotpService {

    private static final String ISSUER = "Neo4flix";

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();

    public String newSecret() {
        return secretGenerator.generate();
    }

    public QrData qrData(String secret, String username) {
        return new QrData.Builder()
            .label(username)
            .secret(secret)
            .issuer(ISSUER)
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build();
    }

    public String qrImageDataUri(QrData data) {
        try {
            return getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
        } catch (QrGenerationException e) {
            throw new IllegalStateException("QR code generation failed", e);
        }
    }

    public boolean isValidCode(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }
}

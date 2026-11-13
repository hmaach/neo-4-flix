package com.neo4flix.user;

import java.time.Instant;
import java.util.List;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

// No @Relationship fields on purpose: saving a User never touches its RATED or WATCHLISTED edges.
// Those are written with explicit Cypher (UserRepository) so a save cannot delete edges it did not load.
@Node("User")
public class User {

    @Id
    private final String id;
    private String username;
    private String email;
    private String passwordHash;
    // AES-GCM ciphertext of the TOTP secret. Set by 2FA setup, kept while 2FA is enabled.
    private String totpSecretEnc;
    private boolean totpEnabled;
    private List<String> roles;
    private final Instant createdAt;

    public User(String id, String username, String email, String passwordHash, String totpSecretEnc,
                boolean totpEnabled, List<String> roles, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.totpSecretEnc = totpSecretEnc;
        this.totpEnabled = totpEnabled;
        this.roles = roles;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getTotpSecretEnc() {
        return totpSecretEnc;
    }

    public void setTotpSecretEnc(String totpSecretEnc) {
        this.totpSecretEnc = totpSecretEnc;
    }

    public boolean isTotpEnabled() {
        return totpEnabled;
    }

    public void setTotpEnabled(boolean totpEnabled) {
        this.totpEnabled = totpEnabled;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

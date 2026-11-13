package com.neo4flix.twofactor;

// otpauthUri: for manual entry or deep link on a phone. qrImageDataUri: PNG for an <img src>.
public record TwoFactorSetupResponse(String otpauthUri, String qrImageDataUri) {
}

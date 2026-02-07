package com.ealth.codeleat.services;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class VerificationIdGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64UrlEncoder = Base64.getUrlEncoder().withoutPadding();
    private static final int DEFAULT_BYTE_LENGTH = 16;

    public String generateVerificationId() {
        byte[] randomBytes = new byte[DEFAULT_BYTE_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return base64UrlEncoder.encodeToString(randomBytes);
    }
}

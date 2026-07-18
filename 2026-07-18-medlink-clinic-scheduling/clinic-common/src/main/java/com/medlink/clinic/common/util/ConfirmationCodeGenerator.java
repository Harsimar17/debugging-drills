package com.medlink.clinic.common.util;

import java.security.SecureRandom;

/**
 * Generates short, human-readable confirmation codes for patient-facing
 * appointment confirmations (e.g. "MED-7F3K9A").
 */
public final class ConfirmationCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private ConfirmationCodeGenerator() {
    }

    public static String generate() {
        StringBuilder sb = new StringBuilder("MED-");
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}

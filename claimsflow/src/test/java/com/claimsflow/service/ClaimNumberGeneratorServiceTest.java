package com.claimsflow.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaimNumberGeneratorServiceTest {

    private final ClaimNumberGeneratorService generator = new ClaimNumberGeneratorService();

    @Test
    void generatesSequentialNumbersForSameDay() {
        String first = generator.generateClaimNumber();
        String second = generator.generateClaimNumber();
        String third = generator.generateClaimNumber();

        assertNotEquals(first, second);
        assertNotEquals(second, third);
    }

    @Test
    void followsExpectedFormat() {
        String claimNumber = generator.generateClaimNumber();
        assertTrue(claimNumber.matches("CLM-\\d{8}-\\d{4}"));
    }
}

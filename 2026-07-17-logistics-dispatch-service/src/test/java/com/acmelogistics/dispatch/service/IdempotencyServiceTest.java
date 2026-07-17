package com.acmelogistics.dispatch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyServiceTest {

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService();
        ReflectionTestUtils.setField(idempotencyService, "retentionMinutes", 30L);
    }

    @Test
    void firstRequestIsNotADuplicate() {
        assertThat(idempotencyService.isDuplicate("key-1")).isFalse();
    }

    @Test
    void secondRequestWithSameKeyIsADuplicate() {
        idempotencyService.markProcessed("key-2");
        assertThat(idempotencyService.isDuplicate("key-2")).isTrue();
    }

    @Test
    void differentKeysAreIndependent() {
        idempotencyService.markProcessed("key-3");
        assertThat(idempotencyService.isDuplicate("key-4")).isFalse();
    }
}

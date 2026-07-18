package com.medlink.clinic.app.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Availability lookups are read thousands of times per hour for the same
 * handful of (provider, date) pairs during business hours, so we keep the
 * hot set in-process instead of round-tripping to Postgres on every read.
 * A short TTL keeps staleness bounded even without an explicit eviction.
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("providerAvailability");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(5_000)
                .expireAfterWrite(30, TimeUnit.SECONDS));
        return cacheManager;
    }
}

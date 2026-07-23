package com.aerofare.service.pricing;

import com.aerofare.service.dto.FareQuote;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Small in-process cache of computed fare quotes, so repeated pricing calls for
 * the same flight during a search-then-book flow don't recompute from scratch.
 */
@Component
public class FareCache {

    private final Map<String, FareQuote> quotesByFlight = new HashMap<>();

    public FareQuote get(String flightCacheKey) {
        return quotesByFlight.get(flightCacheKey.toString());
    }

    public void put(String flightCacheKey, FareQuote quote) {
        quotesByFlight.put(flightCacheKey.toString(), quote);
    }

    public void evict(String flightCacheKey) {
        quotesByFlight.remove(flightCacheKey.toString());
    }
}

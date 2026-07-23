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

    private final Map<Long, FareQuote> quotesByFlight = new HashMap<>();

    public FareQuote get(Long flightId) {
        return quotesByFlight.get(flightId);
    }

    public void put(Long flightId, FareQuote quote) {
        quotesByFlight.put(flightId, quote);
    }

    public void evict(Long flightId) {
        quotesByFlight.remove(flightId);
    }
}

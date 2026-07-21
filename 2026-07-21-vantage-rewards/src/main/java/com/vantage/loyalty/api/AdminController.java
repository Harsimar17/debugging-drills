package com.vantage.loyalty.api;

import com.vantage.loyalty.service.PointsExpiryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PointsExpiryService expiryService;

    public AdminController(PointsExpiryService expiryService) {
        this.expiryService = expiryService;
    }

    /**
     * Manually trigger the points expiry sweep (support tooling).
     */
    @PostMapping("/expiry/run")
    public Map<String, Object> runExpiry() {
        int expired = expiryService.sweepExpiredPoints();
        return Map.of("expiredEntries", expired);
    }
}

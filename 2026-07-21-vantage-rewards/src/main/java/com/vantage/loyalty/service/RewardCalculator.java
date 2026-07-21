package com.vantage.loyalty.service;

import com.vantage.loyalty.config.RewardProperties;
import com.vantage.loyalty.domain.enums.MembershipTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

/**
 * Pure calculation helpers for the rewards engine: turning spend into points and
 * points into an estimated cash value. Kept free of persistence concerns so the
 * rules are easy to unit test.
 */
@Component
public class RewardCalculator {

    private final RewardProperties properties;

    /**
     * Tier bonus multipliers applied on top of the base earn rate. Higher tiers
     * accrue points faster on the same spend.
     */
    private static final Map<MembershipTier, BigDecimal> TIER_MULTIPLIERS = new EnumMap<>(MembershipTier.class);

    static {
        TIER_MULTIPLIERS.put(MembershipTier.STANDARD, new BigDecimal("1.00"));
        TIER_MULTIPLIERS.put(MembershipTier.SILVER, new BigDecimal("1.25"));
        TIER_MULTIPLIERS.put(MembershipTier.GOLD, new BigDecimal("1.50"));
        TIER_MULTIPLIERS.put(MembershipTier.PLATINUM, new BigDecimal("1.75"));
    }

    public RewardCalculator(RewardProperties properties) {
        this.properties = properties;
    }

    /**
     * Points earned for a spend amount, rounded down to whole points, applying
     * the base earn rate and the member's tier multiplier.
     */
    public long pointsForSpend(BigDecimal spendAmount, MembershipTier tier) {
    	
        BigDecimal multiplier = TIER_MULTIPLIERS.get(tier);
        BigDecimal raw = null;
        try {
        raw = spendAmount
                .multiply(properties.getBaseEarnRate())
                .multiply(multiplier);
        }
        catch (Exception e) {
			// TODO: handle exception
        	e.printStackTrace();
		}
        return raw.setScale(0, RoundingMode.DOWN).longValueExact();
    }

    /**
     * Estimated monetary value of a points balance, using the configured
     * redemption conversion rate (points required per currency unit).
     */
    public BigDecimal pointsToCurrency(long points) {
    	BigDecimal conversion = BigDecimal.valueOf(properties.getRedemptionConversionRate());

    	return BigDecimal.valueOf(points)
    	        .divide(conversion, 2, RoundingMode.HALF_UP);
    }

    /**
     * Points required to fully cover the given catalog cost. Catalog items are
     * already priced in points, so this is an identity today but is centralised
     * for future promotional discounting.
     */
    public long pointsForCatalogCost(long pointsCost) {
        return pointsCost;
    }
}

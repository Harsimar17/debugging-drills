package com.vantage.loyalty.service;

import com.vantage.loyalty.config.RewardProperties;
import com.vantage.loyalty.domain.Member;
import com.vantage.loyalty.domain.PointsLedgerEntry;
import com.vantage.loyalty.domain.enums.LedgerEntryType;
import com.vantage.loyalty.domain.enums.MembershipTier;
import com.vantage.loyalty.dto.EarnPointsRequest;
import com.vantage.loyalty.exception.LoyaltyException;
import com.vantage.loyalty.exception.ResourceNotFoundException;
import com.vantage.loyalty.repository.MemberRepository;
import com.vantage.loyalty.repository.PointsLedgerEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class EarnService {

    private static final Logger log = LoggerFactory.getLogger(EarnService.class);

    private final MemberRepository memberRepository;
    private final PointsLedgerEntryRepository ledgerRepository;
    private final RewardCalculator calculator;
    private final RewardProperties properties;

    public EarnService(MemberRepository memberRepository,
                       PointsLedgerEntryRepository ledgerRepository,
                       RewardCalculator calculator,
                       RewardProperties properties) {
        this.memberRepository = memberRepository;
        this.ledgerRepository = ledgerRepository;
        this.calculator = calculator;
        this.properties = properties;
    }

    @Transactional
    public List<PointsLedgerEntry> earn(Long memberId, EarnPointsRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));

        if (ledgerRepository.existsByMemberIdAndSourceReferenceAndEntryType(
                memberId, request.getSourceReference(), LedgerEntryType.EARN)) {
            log.info("Earn for member {} reference {} already processed; skipping",
                    memberId, request.getSourceReference());
            return List.of();
        }

        MembershipTier tier = member.getTier();
        long basePoints = calculator.pointsForSpend(request.getSpendAmount(), MembershipTier.STANDARD);
        long tierPoints = calculator.pointsForSpend(request.getSpendAmount(), tier);
        long bonusPoints = tierPoints - basePoints;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMonths(properties.getPointsValidityMonths());

        // Assemble the entries for this earn event. A set guards against an entry
        // being accidentally staged twice within the same request before we persist.
        List<PointsLedgerEntry> entries = new ArrayList<>();
        entries.add(new PointsLedgerEntry(memberId, LedgerEntryType.EARN, basePoints,
                describe(request, "base"), request.getSourceReference(), now, expiresAt));

        if (bonusPoints > 0) {
            entries.add(new PointsLedgerEntry(memberId, LedgerEntryType.EARN, bonusPoints,
                    describe(request, tier + " tier bonus"), request.getSourceReference(), now, expiresAt));
        }

        if (entries.isEmpty()) {
            throw new LoyaltyException("No points accrued for spend " + request.getSpendAmount());
        }

        List<PointsLedgerEntry> saved = ledgerRepository.saveAll(entries);
        log.info("Credited {} ledger entrie(s) totalling {} points to member {} (tier {})",
                saved.size(), basePoints + Math.max(bonusPoints, 0), memberId, tier);
        return saved;
    }

    private String describe(EarnPointsRequest request, String suffix) {
        String base = request.getDescription() != null ? request.getDescription() : "Points earned";
        return base + " (" + suffix + ")";
    }
}

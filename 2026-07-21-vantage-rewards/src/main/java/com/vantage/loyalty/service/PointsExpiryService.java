package com.vantage.loyalty.service;

import com.vantage.loyalty.config.RewardProperties;
import com.vantage.loyalty.domain.PointsLedgerEntry;
import com.vantage.loyalty.domain.enums.LedgerEntryType;
import com.vantage.loyalty.repository.PointsLedgerEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Sweeps EARN entries whose validity window has elapsed, marking them expired
 * and writing a compensating EXPIRY entry so the balance reflects the loss.
 */
@Service
public class PointsExpiryService {

    private static final Logger log = LoggerFactory.getLogger(PointsExpiryService.class);

    private final PointsLedgerEntryRepository ledgerRepository;
    private final RewardProperties properties;

    public PointsExpiryService(PointsLedgerEntryRepository ledgerRepository,
                               RewardProperties properties) {
        this.ledgerRepository = ledgerRepository;
        this.properties = properties;
    }

    @Transactional
    public int sweepExpiredPoints() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneOffset.UTC).minusDays(properties.getExpiryGraceDays());
        List<PointsLedgerEntry> expirable = ledgerRepository.findExpirable(cutoff);
        log.info("Expiry sweep starting; cutoff={} candidates={}", cutoff, expirable.size());

        List<PointsLedgerEntry> toPersist = new ArrayList<>();
        int expiredCount = 0;
        for (PointsLedgerEntry entry : expirable) {
            entry.markExpired();
            toPersist.add(entry);
            toPersist.add(new PointsLedgerEntry(entry.getMemberId(), LedgerEntryType.EXPIRY,
                    -entry.getPoints(), "Points expired", entry.getSourceReference(),
                    LocalDateTime.now(), null));
            expiredCount++;
        }
        ledgerRepository.saveAll(toPersist);
        log.info("Expiry sweep complete; expired {} ledger entrie(s)", expiredCount);
        return expiredCount;
    }
}

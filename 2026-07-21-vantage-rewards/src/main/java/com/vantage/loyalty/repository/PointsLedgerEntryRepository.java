package com.vantage.loyalty.repository;

import com.vantage.loyalty.domain.PointsLedgerEntry;
import com.vantage.loyalty.domain.enums.LedgerEntryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PointsLedgerEntryRepository extends JpaRepository<PointsLedgerEntry, Long> {

    Page<PointsLedgerEntry> findByMemberIdOrderByEarnedAtDesc(Long memberId, Pageable pageable);

    boolean existsByMemberIdAndSourceReferenceAndEntryType(Long memberId, String sourceReference,
                                                           LedgerEntryType entryType);

    /**
     * Unexpired EARN entries whose validity window has elapsed and which should
     * therefore be swept by the nightly expiry job.
     */
    @Query("SELECT e FROM PointsLedgerEntry e "
            + "WHERE e.expired = false "
            + "AND e.entryType = com.vantage.loyalty.domain.enums.LedgerEntryType.EARN "
            + "AND e.expiresAt IS NOT NULL "
            + "AND e.expiresAt < :cutoff")
    List<PointsLedgerEntry> findExpirable(@Param("cutoff") LocalDateTime cutoff);
    
    List<PointsLedgerEntry> findByMemberId(Long memberId);
}

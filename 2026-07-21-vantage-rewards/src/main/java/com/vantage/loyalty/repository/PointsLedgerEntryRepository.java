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
     * Current available points balance for a member: the sum of active,
     * unexpired points the member can still spend.
     */
    @Query("SELECT COALESCE(SUM(e.points), 0) FROM PointsLedgerEntry e "
            + "WHERE e.memberId = :memberId "
            + "AND e.expired = false "
            + "AND e.entryType = :type")
    long currentBalance(@Param("memberId") Long memberId, @Param("type")LedgerEntryType type);

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
}

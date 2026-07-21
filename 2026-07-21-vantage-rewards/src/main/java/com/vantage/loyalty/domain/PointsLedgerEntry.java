package com.vantage.loyalty.domain;

import com.vantage.loyalty.domain.enums.LedgerEntryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single, immutable movement in a member's points ledger.
 *
 * <p>Signed convention: EARN and positive ADJUSTMENT entries carry a positive
 * {@code points} value; REDEEM, EXPIRY and negative ADJUSTMENT entries carry a
 * negative value. The running balance is therefore the signed sum of a member's
 * entries.</p>
 */
@Entity
@Table(name = "points_ledger_entry",
        indexes = {
                @Index(name = "idx_ledger_member", columnList = "member_id"),
                @Index(name = "idx_ledger_expires", columnList = "expires_at")
        })
public class PointsLedgerEntry {

    public void setPoints(long points) {
		this.points = points;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 20)
    private LedgerEntryType entryType;

    @Column(name = "points", nullable = false)
    private long points;

    @Column(name = "description")
    private String description;

    /**
     * Optional idempotency reference for the originating business event
     * (e.g. an order id). Two earn events for the same reference must not
     * both credit points.
     */
    @Column(name = "source_reference", length = 80)
    private String sourceReference;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "expired", nullable = false)
    private boolean expired;

    protected PointsLedgerEntry() {
    }

    public PointsLedgerEntry(Long memberId, LedgerEntryType entryType, long points,
                             String description, String sourceReference,
                             LocalDateTime earnedAt, LocalDateTime expiresAt) {
        this.memberId = memberId;
        this.entryType = entryType;
        this.points = points;
        this.description = description;
        this.sourceReference = sourceReference;
        this.earnedAt = earnedAt;
        this.expiresAt = expiresAt;
        this.expired = false;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public long getPoints() {
        return points;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return expired;
    }

    public void markExpired() {
        this.expired = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PointsLedgerEntry)) {
            return false;
        }
        PointsLedgerEntry that = (PointsLedgerEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

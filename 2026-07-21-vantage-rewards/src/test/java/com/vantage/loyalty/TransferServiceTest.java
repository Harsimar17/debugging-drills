package com.vantage.loyalty;

import com.vantage.loyalty.domain.Member;
import com.vantage.loyalty.domain.PointsLedgerEntry;
import com.vantage.loyalty.domain.enums.LedgerEntryType;
import com.vantage.loyalty.domain.enums.MemberStatus;
import com.vantage.loyalty.domain.enums.MembershipTier;
import com.vantage.loyalty.dto.TransferPointsRequest;
import com.vantage.loyalty.exception.InsufficientPointsException;
import com.vantage.loyalty.exception.ResourceNotFoundException;
import com.vantage.loyalty.repository.MemberRepository;
import com.vantage.loyalty.repository.PointsLedgerEntryRepository;
import com.vantage.loyalty.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Acceptance tests for VANTAGE-REQ-118 (peer-to-peer points transfer).
 *
 * <p>These encode the acceptance criteria from the README. They are expected to
 * be RED until {@link TransferService#transfer} is implemented, and to turn
 * GREEN once the feature is built. Each test maps to one criterion so a partial
 * implementation gives precise feedback.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class TransferServiceTest {

    @Autowired
    private TransferService transferService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PointsLedgerEntryRepository ledgerRepository;

    private static final AtomicInteger SEQ = new AtomicInteger(90_000);

    // --------------------------------------------------------------------- //
    // Criteria 2 & 3: atomic debit + credit recorded as ADJUSTMENT entries
    // --------------------------------------------------------------------- //
    @Test
    void transferDebitsSenderAndCreditsRecipient() {
        Member sender = newMember(MembershipTier.GOLD, MemberStatus.ACTIVE);
        Member recipient = newMember(MembershipTier.STANDARD, MemberStatus.ACTIVE);
        grantPoints(sender, 1000, LocalDateTime.now().plusMonths(6).withNano(0));

        transferService.transfer(sender.getId(), request(recipient.getMemberNumber(), 400));

        List<PointsLedgerEntry> senderAdjustments = adjustments(sender.getId());
        List<PointsLedgerEntry> recipientAdjustments = adjustments(recipient.getId());

        assertEquals(1, senderAdjustments.size(), "sender should have one ADJUSTMENT debit");
        assertEquals(-400, senderAdjustments.get(0).getPoints(), "sender is debited the points");

        assertEquals(1, recipientAdjustments.size(), "recipient should have one ADJUSTMENT credit");
        assertEquals(400, recipientAdjustments.get(0).getPoints(), "recipient is credited the points");
    }

    // --------------------------------------------------------------------- //
    // Criterion 3: each ledger entry references the counterparty
    // --------------------------------------------------------------------- //
    @Test
    void ledgerEntriesReferenceTheCounterparty() {
        Member sender = newMember(MembershipTier.SILVER, MemberStatus.ACTIVE);
        Member recipient = newMember(MembershipTier.STANDARD, MemberStatus.ACTIVE);
        grantPoints(sender, 800, LocalDateTime.now().plusMonths(4).withNano(0));

        transferService.transfer(sender.getId(), request(recipient.getMemberNumber(), 250));

        String senderNote = adjustments(sender.getId()).get(0).getDescription();
        String recipientNote = adjustments(recipient.getId()).get(0).getDescription();

        assertTrue(referencesCounterparty(senderNote, recipient),
                "sender entry should reference the recipient, was: " + senderNote);
        assertTrue(referencesCounterparty(recipientNote, sender),
                "recipient entry should reference the sender, was: " + recipientNote);
    }

    // --------------------------------------------------------------------- //
    // Criterion 4: transferred points inherit the sender's remaining validity
    // window (they must NOT be reset to a fresh full-length expiry)
    // --------------------------------------------------------------------- //
    @Test
    void transferredPointsInheritRemainingValidity() {
        Member sender = newMember(MembershipTier.STANDARD, MemberStatus.ACTIVE);
        Member recipient = newMember(MembershipTier.STANDARD, MemberStatus.ACTIVE);
        LocalDateTime senderExpiry = LocalDateTime.now().plusMonths(2).withNano(0);
        grantPoints(sender, 500, senderExpiry);

        transferService.transfer(sender.getId(), request(recipient.getMemberNumber(), 500));

        LocalDateTime creditedExpiry = adjustments(recipient.getId()).get(0).getExpiresAt();
        assertEquals(senderExpiry, creditedExpiry,
                "credited points must keep the sender's remaining validity, not a fresh window");
    }

    // --------------------------------------------------------------------- //
    // Criterion 1: reject when the sender lacks sufficient points, with no
    // ledger side effects on either side
    // --------------------------------------------------------------------- //
    @Test
    void rejectsWhenSenderHasInsufficientPoints() {
        Member sender = newMember(MembershipTier.STANDARD, MemberStatus.ACTIVE);
        Member recipient = newMember(MembershipTier.STANDARD, MemberStatus.ACTIVE);
        grantPoints(sender, 100, LocalDateTime.now().plusMonths(6).withNano(0));

        assertThrows(InsufficientPointsException.class,
                () -> transferService.transfer(sender.getId(), request(recipient.getMemberNumber(), 500)));

        assertTrue(adjustments(sender.getId()).isEmpty(), "no debit should be recorded on failure");
        assertTrue(adjustments(recipient.getId()).isEmpty(), "no credit should be recorded on failure");
    }

    // --------------------------------------------------------------------- //
    // Criterion 5: recipient must exist
    // --------------------------------------------------------------------- //
    @Test
    void rejectsUnknownRecipient() {
        Member sender = newMember(MembershipTier.STANDARD, MemberStatus.ACTIVE);
        grantPoints(sender, 1000, LocalDateTime.now().plusMonths(6).withNano(0));

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.transfer(sender.getId(), request("VG000000", 100)));
    }

    // --------------------------------------------------------------------- //
    // Criterion 5: a member cannot transfer to themselves
    // --------------------------------------------------------------------- //
    @Test
    void rejectsSelfTransfer() {
        Member sender = newMember(MembershipTier.STANDARD, MemberStatus.ACTIVE);
        grantPoints(sender, 1000, LocalDateTime.now().plusMonths(6).withNano(0));

        assertThrows(RuntimeException.class,
                () -> transferService.transfer(sender.getId(), request(sender.getMemberNumber(), 100)));

        assertTrue(adjustments(sender.getId()).isEmpty(), "self-transfer must record no movements");
    }

    // --------------------------------------------------------------------- //
    // Criterion 5: recipient must be ACTIVE
    // --------------------------------------------------------------------- //
    @Test
    void rejectsInactiveRecipient() {
        Member sender = newMember(MembershipTier.STANDARD, MemberStatus.ACTIVE);
        Member recipient = newMember(MembershipTier.STANDARD, MemberStatus.SUSPENDED);
        grantPoints(sender, 1000, LocalDateTime.now().plusMonths(6).withNano(0));

        assertThrows(RuntimeException.class,
                () -> transferService.transfer(sender.getId(), request(recipient.getMemberNumber(), 100)));
    }

    // --------------------------------------------------------------------- //
    // Helpers
    // --------------------------------------------------------------------- //
    private Member newMember(MembershipTier tier, MemberStatus status) {
        int n = SEQ.incrementAndGet();
        Member member = new Member("VG" + n, "Member " + n, "member" + n + "@example.com", tier);
        member.setStatus(status);
        return memberRepository.saveAndFlush(member);
    }

    private void grantPoints(Member member, long points, LocalDateTime expiresAt) {
        ledgerRepository.saveAndFlush(new PointsLedgerEntry(
                member.getId(), LedgerEntryType.EARN, points,
                "Seed grant", "SEED-" + SEQ.incrementAndGet(),
                LocalDateTime.now().withNano(0), expiresAt));
    }

    private List<PointsLedgerEntry> adjustments(Long memberId) {
        return ledgerRepository.findByMemberIdOrderByEarnedAtDesc(memberId, Pageable.unpaged())
                .getContent().stream()
                .filter(e -> e.getEntryType() == LedgerEntryType.ADJUSTMENT)
                .toList();
    }

    private boolean referencesCounterparty(String note, Member counterparty) {
        if (note == null) {
            return false;
        }
        return note.contains(counterparty.getMemberNumber())
                || note.contains(counterparty.getFullName());
    }

    private TransferPointsRequest request(String toMemberNumber, long points) {
        TransferPointsRequest request = new TransferPointsRequest();
        request.setToMemberNumber(toMemberNumber);
        request.setPoints(points);
        request.setNote("Repro transfer");
        return request;
    }
}

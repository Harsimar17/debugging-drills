package com.vantage.loyalty;

import com.vantage.loyalty.config.RewardProperties;
import com.vantage.loyalty.domain.Member;
import com.vantage.loyalty.domain.PointsLedgerEntry;
import com.vantage.loyalty.domain.enums.LedgerEntryType;
import com.vantage.loyalty.domain.enums.MembershipTier;
import com.vantage.loyalty.dto.EarnPointsRequest;
import com.vantage.loyalty.repository.MemberRepository;
import com.vantage.loyalty.repository.PointsLedgerEntryRepository;
import com.vantage.loyalty.service.EarnService;
import com.vantage.loyalty.service.RewardCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EarnServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PointsLedgerEntryRepository ledgerRepository;

    private RewardCalculator calculator;
    private EarnService earnService;

    private void init() {
        RewardProperties props = new RewardProperties();
        calculator = new RewardCalculator(props);
        earnService = new EarnService(memberRepository, ledgerRepository, calculator, props);
    }

    @Test
    void standardMemberEarnsSingleBaseEntry() {
        init();
        Member member = new Member("VG100001", "Ava", "ava@example.com", MembershipTier.STANDARD);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(ledgerRepository.existsByMemberIdAndSourceReferenceAndEntryType(any(), any(), any()))
                .thenReturn(false);
        when(ledgerRepository.saveAll(any())).thenAnswer(inv -> {
            Iterable<PointsLedgerEntry> it = inv.getArgument(0);
            return List.copyOf((java.util.Collection<PointsLedgerEntry>) it);
        });

        EarnPointsRequest request = new EarnPointsRequest();
        request.setSpendAmount(new BigDecimal("50.00"));
        request.setSourceReference("ORDER-1");

        List<PointsLedgerEntry> entries = earnService.earn(1L, request);

        assertEquals(1, entries.size());
        assertEquals(LedgerEntryType.EARN, entries.get(0).getEntryType());
        assertEquals(50L, entries.get(0).getPoints());
    }

    @Test
    void duplicateSourceReferenceIsSkipped() {
        init();
        Member member = new Member("VG100001", "Ava", "ava@example.com", MembershipTier.STANDARD);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(ledgerRepository.existsByMemberIdAndSourceReferenceAndEntryType(any(), any(), any()))
                .thenReturn(true);

        EarnPointsRequest request = new EarnPointsRequest();
        request.setSpendAmount(new BigDecimal("50.00"));
        request.setSourceReference("ORDER-1");

        List<PointsLedgerEntry> entries = earnService.earn(1L, request);
        assertEquals(0, entries.size());
    }
}

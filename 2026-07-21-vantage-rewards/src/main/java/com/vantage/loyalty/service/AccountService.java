package com.vantage.loyalty.service;

import com.vantage.loyalty.domain.Member;
import com.vantage.loyalty.domain.PointsLedgerEntry;
import com.vantage.loyalty.domain.enums.LedgerEntryType;
import com.vantage.loyalty.dto.AccountSummaryDto;
import com.vantage.loyalty.dto.LedgerEntryDto;
import com.vantage.loyalty.exception.ResourceNotFoundException;
import com.vantage.loyalty.mapper.LedgerMapper;
import com.vantage.loyalty.repository.MemberRepository;
import com.vantage.loyalty.repository.PointsLedgerEntryRepository;
import com.vantage.loyalty.util.CalculatorUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final MemberRepository memberRepository;
    private final PointsLedgerEntryRepository ledgerRepository;
    private final RewardCalculator calculator;
    private final LedgerMapper ledgerMapper;
    
    @Autowired
    private CalculatorUtil util;

    public AccountService(MemberRepository memberRepository,
                          PointsLedgerEntryRepository ledgerRepository,
                          RewardCalculator calculator,
                          LedgerMapper ledgerMapper) {
        this.memberRepository = memberRepository;
        this.ledgerRepository = ledgerRepository;
        this.calculator = calculator;
        this.ledgerMapper = ledgerMapper;
    }

    @Transactional(readOnly = true)
    public AccountSummaryDto summarise(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));

        long balance = util.calculateTotalBalance(memberId);

        AccountSummaryDto dto = new AccountSummaryDto();
        dto.setMemberId(member.getId());
        dto.setMemberNumber(member.getMemberNumber());
        dto.setFullName(member.getFullName());
        dto.setTier(member.getTier());
        dto.setAvailablePoints(balance);
        dto.setEstimatedCashValue(calculator.pointsToCurrency(balance));
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntryDto> ledger(Long memberId, Pageable pageable) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found: " + memberId);
        }
        Page<PointsLedgerEntry> page = ledgerRepository.findByMemberIdOrderByEarnedAtDesc(memberId, pageable);
        return page.map(ledgerMapper::toDto);
    }
}

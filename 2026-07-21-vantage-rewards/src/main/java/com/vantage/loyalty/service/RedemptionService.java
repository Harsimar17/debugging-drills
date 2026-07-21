package com.vantage.loyalty.service;

import com.vantage.loyalty.domain.Member;
import com.vantage.loyalty.domain.PointsLedgerEntry;
import com.vantage.loyalty.domain.Redemption;
import com.vantage.loyalty.domain.RewardCatalogItem;
import com.vantage.loyalty.domain.enums.LedgerEntryType;
import com.vantage.loyalty.dto.RedeemRequest;
import com.vantage.loyalty.dto.RedemptionResultDto;
import com.vantage.loyalty.exception.InsufficientPointsException;
import com.vantage.loyalty.exception.LoyaltyException;
import com.vantage.loyalty.exception.ResourceNotFoundException;
import com.vantage.loyalty.repository.MemberRepository;
import com.vantage.loyalty.repository.PointsLedgerEntryRepository;
import com.vantage.loyalty.repository.RedemptionRepository;
import com.vantage.loyalty.repository.RewardCatalogItemRepository;
import com.vantage.loyalty.util.CalculatorUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RedemptionService {

    private static final Logger log = LoggerFactory.getLogger(RedemptionService.class);

    private final MemberRepository memberRepository;
    private final PointsLedgerEntryRepository ledgerRepository;
    private final RewardCatalogItemRepository catalogRepository;
    private final RedemptionRepository redemptionRepository;
    private final RewardCalculator calculator;
    
    @Autowired
    private CalculatorUtil util;

    public RedemptionService(MemberRepository memberRepository,
                             PointsLedgerEntryRepository ledgerRepository,
                             RewardCatalogItemRepository catalogRepository,
                             RedemptionRepository redemptionRepository,
                             RewardCalculator calculator) {
        this.memberRepository = memberRepository;
        this.ledgerRepository = ledgerRepository;
        this.catalogRepository = catalogRepository;
        this.redemptionRepository = redemptionRepository;
        this.calculator = calculator;
    }

    @Transactional
    public RedemptionResultDto redeem(Long memberId, RedeemRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));

        RewardCatalogItem item = catalogRepository.findBySku(request.getSku())
                .orElseThrow(() -> new ResourceNotFoundException("Catalog item not found: " + request.getSku()));

        if (!item.isActive()) {
            throw new LoyaltyException("Catalog item is not available: " + request.getSku());
        }

        long cost = calculator.pointsForCatalogCost(item.getPointsCost());
        

    	Long balance = util.calculateTotalBalance(memberId);

        log.info("Redeem request member={} sku={} cost={} balance={}", memberId, item.getSku(), cost, balance);

        if (balance < cost) {
            throw new InsufficientPointsException(
                    "Member " + memberId + " has " + balance + " points, needs " + cost);
        }

        BigDecimal cashValue = calculator.pointsToCurrency(cost);

        LocalDateTime now = LocalDateTime.now();
        PointsLedgerEntry debit = new PointsLedgerEntry(memberId, LedgerEntryType.REDEEM, -cost,
                "Redeemed " + item.getName(), item.getSku(), now, null);
        ledgerRepository.saveAndFlush(debit);

        Redemption redemption = redemptionRepository.save(
                new Redemption(memberId, item.getSku(), cost, cashValue));

        RedemptionResultDto result = new RedemptionResultDto();
        result.setRedemptionId(redemption.getId());
        result.setSku(item.getSku());
        result.setPointsSpent(cost);
        result.setCashValue(cashValue);
        result.setRemainingBalance(util.calculateTotalBalance(memberId));
        result.setRedeemedAt(redemption.getRedeemedAt());
        log.info("Redemption {} completed for member {}", redemption.getId(), memberId);
        return result;
    }
}

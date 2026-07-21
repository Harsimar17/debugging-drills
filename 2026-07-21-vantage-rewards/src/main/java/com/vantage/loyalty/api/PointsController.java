package com.vantage.loyalty.api;

import com.vantage.loyalty.domain.PointsLedgerEntry;
import com.vantage.loyalty.dto.EarnPointsRequest;
import com.vantage.loyalty.dto.LedgerEntryDto;
import com.vantage.loyalty.dto.RedeemRequest;
import com.vantage.loyalty.dto.RedemptionResultDto;
import com.vantage.loyalty.mapper.LedgerMapper;
import com.vantage.loyalty.service.EarnService;
import com.vantage.loyalty.service.RedemptionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members/{memberId}/points")
public class PointsController {

    private final EarnService earnService;
    private final RedemptionService redemptionService;
    private final LedgerMapper ledgerMapper;

    public PointsController(EarnService earnService,
                            RedemptionService redemptionService,
                            LedgerMapper ledgerMapper) {
        this.earnService = earnService;
        this.redemptionService = redemptionService;
        this.ledgerMapper = ledgerMapper;
    }

    @PostMapping("/earn")
    public List<LedgerEntryDto> earn(@PathVariable Long memberId,
                                     @Valid @RequestBody EarnPointsRequest request) {
        List<PointsLedgerEntry> entries = earnService.earn(memberId, request);
        return entries.stream().map(ledgerMapper::toDto).toList();
    }

    @PostMapping("/redeem")
    public RedemptionResultDto redeem(@PathVariable Long memberId,
                                      @Valid @RequestBody RedeemRequest request) {
        return redemptionService.redeem(memberId, request);
    }
}

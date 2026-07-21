package com.vantage.loyalty.api;

import com.vantage.loyalty.dto.AccountSummaryDto;
import com.vantage.loyalty.dto.LedgerEntryDto;
import com.vantage.loyalty.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/{memberId}/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/summary")
    public AccountSummaryDto summary(@PathVariable Long memberId) {
        return accountService.summarise(memberId);
    }

    @GetMapping("/ledger")
    public Page<LedgerEntryDto> ledger(@PathVariable Long memberId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accountService.ledger(memberId, pageable);
    }
}

package com.vantage.loyalty.api;

import com.vantage.loyalty.dto.TransferPointsRequest;
import com.vantage.loyalty.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/{memberId}/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<Void> transfer(@PathVariable Long memberId,
                                         @Valid @RequestBody TransferPointsRequest request) {
        transferService.transfer(memberId, request);
        return ResponseEntity.accepted().build();
    }
}

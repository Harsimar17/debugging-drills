package com.aerofare.app.api;

import com.aerofare.service.refund.RefundResult;
import com.aerofare.service.refund.RefundService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings/{recordLocator}/refund")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping
    public RefundResult refund(@PathVariable String recordLocator) throws Exception {
        return refundService.refund(recordLocator);
    }
}

package com.acme.billing.api;

import com.acme.billing.dto.ApiResponse;
import com.acme.billing.dto.PlanDto;
import com.acme.billing.service.PlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ApiResponse<List<PlanDto>> listPlans() {
        return ApiResponse.ok(planService.listActivePlans());
    }
}

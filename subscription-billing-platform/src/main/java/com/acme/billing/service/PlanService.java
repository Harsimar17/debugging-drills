package com.acme.billing.service;

import com.acme.billing.domain.Plan;
import com.acme.billing.dto.PlanDto;
import com.acme.billing.exception.ResourceNotFoundException;
import com.acme.billing.mapper.PlanMapper;
import com.acme.billing.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    public PlanService(PlanRepository planRepository, PlanMapper planMapper) {
        this.planRepository = planRepository;
        this.planMapper = planMapper;
    }

    @Transactional(readOnly = true)
    public List<PlanDto> listActivePlans() {
        return planRepository.findByActiveTrue().stream()
                .map(planMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Plan getPlanByCode(String code) {
        return planRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + code));
    }
}

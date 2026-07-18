package com.medlink.clinic.api;

import com.medlink.clinic.service.ProviderService;
import com.medlink.clinic.service.dto.ProviderDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping
    public List<ProviderDto> getAll(@RequestParam(required = false) String specialty) {
        if (specialty != null && !specialty.isBlank()) {
            return providerService.getBySpecialty(specialty);
        }
        return providerService.getAll();
    }

    @GetMapping("/{id}")
    public ProviderDto getById(@PathVariable Long id) {
        return providerService.getById(id);
    }
}

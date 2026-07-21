package com.vantage.loyalty.api;

import com.vantage.loyalty.domain.RewardCatalogItem;
import com.vantage.loyalty.repository.RewardCatalogItemRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final RewardCatalogItemRepository catalogRepository;

    public CatalogController(RewardCatalogItemRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @GetMapping
    public List<RewardCatalogItem> list() {
        return catalogRepository.findAll();
    }
}

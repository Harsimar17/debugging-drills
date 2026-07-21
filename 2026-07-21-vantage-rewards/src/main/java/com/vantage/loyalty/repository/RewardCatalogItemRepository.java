package com.vantage.loyalty.repository;

import com.vantage.loyalty.domain.RewardCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RewardCatalogItemRepository extends JpaRepository<RewardCatalogItem, Long> {

    Optional<RewardCatalogItem> findBySku(String sku);
}

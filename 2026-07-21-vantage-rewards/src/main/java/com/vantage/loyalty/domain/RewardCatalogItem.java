package com.vantage.loyalty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reward_catalog_item")
public class RewardCatalogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, unique = true, length = 40)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "points_cost", nullable = false)
    private long pointsCost;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected RewardCatalogItem() {
    }

    public RewardCatalogItem(String sku, String name, long pointsCost) {
        this.sku = sku;
        this.name = name;
        this.pointsCost = pointsCost;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public long getPointsCost() {
        return pointsCost;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

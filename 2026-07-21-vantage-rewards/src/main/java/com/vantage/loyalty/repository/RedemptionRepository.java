package com.vantage.loyalty.repository;

import com.vantage.loyalty.domain.Redemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {

    List<Redemption> findByMemberIdOrderByRedeemedAtDesc(Long memberId);
}

package com.claimsflow.repository;

import com.claimsflow.domain.Adjuster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdjusterRepository extends JpaRepository<Adjuster, Long> {

    List<Adjuster> findByRegion(String region);
}

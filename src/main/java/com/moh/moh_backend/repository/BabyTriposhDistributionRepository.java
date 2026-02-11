package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.BabyTriposhDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BabyTriposhDistributionRepository extends JpaRepository<BabyTriposhDistribution, Integer> {

    List<BabyTriposhDistribution> findByBaby_BabyIdOrderByDistributionDateDesc(Integer babyId);

    @Query("SELECT d FROM BabyTriposhDistribution d WHERE d.baby.motherId = :motherId ORDER BY d.distributionDate DESC")
    List<BabyTriposhDistribution> findByMotherId(@Param("motherId") Integer motherId);
}

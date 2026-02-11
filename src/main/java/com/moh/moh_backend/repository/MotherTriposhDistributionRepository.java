package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.MotherTriposhDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MotherTriposhDistributionRepository extends JpaRepository<MotherTriposhDistribution, Integer> {

    List<MotherTriposhDistribution> findByPregnancy_PregnancyIdOrderByDistributionDateDesc(Integer pregnancyId);

    @Query("SELECT d FROM MotherTriposhDistribution d WHERE d.pregnancy.mother.motherId = :motherId ORDER BY d.distributionDate DESC")
    List<MotherTriposhDistribution> findByMotherId(@Param("motherId") Integer motherId);
}

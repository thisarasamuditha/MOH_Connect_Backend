package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.BabyVaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BabyVaccinationRepository extends JpaRepository<BabyVaccination, Integer> {
    
    List<BabyVaccination> findByBaby_BabyId(Integer babyId);
    
    @Query("SELECT bv FROM BabyVaccination bv WHERE bv.baby.motherId = :motherId")
    List<BabyVaccination> findByMotherId(@Param("motherId") Integer motherId);
    
    @Query("SELECT bv FROM BabyVaccination bv WHERE bv.baby.babyId = :babyId ORDER BY bv.vaccinationDate DESC")
    List<BabyVaccination> findByBabyIdOrderByVaccinationDateDesc(@Param("babyId") Integer babyId);
}

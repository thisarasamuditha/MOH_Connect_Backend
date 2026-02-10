package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.MotherVaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MotherVaccinationRepository extends JpaRepository<MotherVaccination, Integer> {
    
    List<MotherVaccination> findByPregnancy_PregnancyId(Integer pregnancyId);
    
    @Query("SELECT mv FROM MotherVaccination mv WHERE mv.pregnancy.mother.motherId = :motherId")
    List<MotherVaccination> findByMotherId(@Param("motherId") Integer motherId);
    
    @Query("SELECT mv FROM MotherVaccination mv WHERE mv.pregnancy.pregnancyId = :pregnancyId ORDER BY mv.vaccinationDate DESC")
    List<MotherVaccination> findByPregnancyIdOrderByVaccinationDateDesc(@Param("pregnancyId") Integer pregnancyId);
}

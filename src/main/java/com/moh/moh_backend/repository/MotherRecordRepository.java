package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.MotherRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MotherRecordRepository extends JpaRepository<MotherRecord, Integer> {
    List<MotherRecord> findByPregnancy_PregnancyId(Integer pregnancyId);
}

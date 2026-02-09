package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.BabyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BabyRecordRepository extends JpaRepository<BabyRecord, Integer> {
    List<BabyRecord> findByBaby_BabyId(Integer babyId);
}

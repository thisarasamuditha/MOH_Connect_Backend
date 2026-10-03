package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.BabyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BabyRecordRepository extends JpaRepository<BabyRecord, Integer> {
    List<BabyRecord> findByBaby_BabyId(Integer babyId);
    List<BabyRecord> findByBaby_BabyIdOrderByRecordDateDesc(Integer babyId);
    List<BabyRecord> findByBaby_BabyIdOrderByRecordDateAsc(Integer babyId);
    java.util.Optional<BabyRecord> findFirstByBaby_BabyIdOrderByRecordDateDesc(Integer babyId);
    List<BabyRecord> findByBaby_BabyIdIn(List<Integer> babyIds);
    List<BabyRecord> findByBaby_BabyIdInOrderByRecordDateDesc(List<Integer> babyIds);
}

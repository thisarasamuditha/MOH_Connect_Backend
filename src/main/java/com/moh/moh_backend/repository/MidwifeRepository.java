package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.Midwife;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MidwifeRepository extends JpaRepository<Midwife, Integer> {
    <Optional> Midwife findByMidwifeId(Integer midwife_id);
}
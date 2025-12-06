package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.Mother;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotherRepository extends JpaRepository <Mother, Integer> {
    boolean existsByNic(String nic);
}

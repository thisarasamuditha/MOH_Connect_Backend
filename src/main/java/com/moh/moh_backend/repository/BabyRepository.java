package com.moh.moh_backend.repository;


import com.moh.moh_backend.model.Baby;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface BabyRepository extends JpaRepository<Baby, Integer> {
    List<Baby> findByMotherId(Integer motherId);
    List<Baby> findByPregnancyId(Integer pregnancyId);
}
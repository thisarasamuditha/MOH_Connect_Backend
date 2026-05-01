package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.PhmArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhmAreaRepository extends JpaRepository<PhmArea, Integer> {
    Optional<PhmArea> findById(Integer phmAreaId);
}

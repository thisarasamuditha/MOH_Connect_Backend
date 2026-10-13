package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.Midwife;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MidwifeRepository extends JpaRepository<Midwife, Integer> {
    Optional<Midwife> findByUser_UserId(Integer userId);
    List<Midwife> findByPhmArea_PhmAreaId(Integer phmAreaId);
}

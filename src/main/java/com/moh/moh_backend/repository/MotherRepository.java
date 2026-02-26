package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.Mother;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MotherRepository extends JpaRepository <Mother, Integer> {
    boolean existsByNic(String nic);
    List<Mother> findByPhmArea_PhmAreaId(Integer phmAreaId);
    Optional<Mother> findByUser_UserId(Integer userId);
}

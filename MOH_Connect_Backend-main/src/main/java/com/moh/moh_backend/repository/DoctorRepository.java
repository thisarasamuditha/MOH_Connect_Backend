package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    boolean existsByLicenseNumber(String licenseNumber);
    Optional<Doctor> findByUser_UserId(Integer userId);
}
package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionTypeRepository extends JpaRepository<SessionType, Integer> {
    Optional<SessionType> findByTypeName(String typeName);
}

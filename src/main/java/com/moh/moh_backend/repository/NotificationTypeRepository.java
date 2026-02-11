package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTypeRepository extends JpaRepository<NotificationType, Integer> {
    Optional<NotificationType> findByTypeName(String typeName);
}

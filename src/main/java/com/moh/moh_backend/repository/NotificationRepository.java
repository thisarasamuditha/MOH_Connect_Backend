package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.Notification;
import com.moh.moh_backend.model.Notification.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByMother_MotherIdOrderBySentDateDesc(Integer motherId);

    List<Notification> findByMother_MotherIdAndStatusOrderBySentDateDesc(Integer motherId, NotificationStatus status);

    List<Notification> findByMidwife_MidwifeIdOrderBySentDateDesc(Integer midwifeId);

    List<Notification> findByStatusOrderBySentDateDesc(NotificationStatus status);

    List<Notification> findByMother_MotherIdAndReadAtIsNullOrderBySentDateDesc(Integer motherId);

    long countByMother_MotherIdAndReadAtIsNull(Integer motherId);
}

package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.NotificationDtos.NotificationCreateRequest;
import com.moh.moh_backend.dto.NotificationDtos.NotificationResponse;
import com.moh.moh_backend.dto.NotificationDtos.UnreadCountResponse;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.Notification;
import com.moh.moh_backend.model.Notification.DeliveryMethod;
import com.moh.moh_backend.model.Notification.NotificationStatus;
import com.moh.moh_backend.model.NotificationType;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.NotificationRepository;
import com.moh.moh_backend.repository.NotificationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository typeRepository;
    private final MotherRepository motherRepository;
    private final MidwifeRepository midwifeRepository;

    @Transactional
    public NotificationResponse create(NotificationCreateRequest request) {
        if (request.getTypeId() == null) {
            throw new IllegalArgumentException("Notification type ID is required");
        }
        if (request.getMotherId() == null) {
            throw new IllegalArgumentException("Mother ID is required");
        }
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }

        NotificationType type = typeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Notification type not found with id: " + request.getTypeId()));

        Mother mother = motherRepository.findById(request.getMotherId())
                .orElseThrow(() -> new IllegalArgumentException("Mother not found with id: " + request.getMotherId()));

        Midwife midwife = null;
        if (request.getMidwifeId() != null) {
            midwife = midwifeRepository.findById(request.getMidwifeId())
                    .orElseThrow(() -> new IllegalArgumentException("Midwife not found with id: " + request.getMidwifeId()));
        }

        Notification notification = Notification.builder()
                .notificationType(type)
                .mother(mother)
                .midwife(midwife)
                .message(request.getMessage())
                .deliveryMethod(request.getDeliveryMethod() != null
                        ? DeliveryMethod.valueOf(request.getDeliveryMethod()) : null)
                .eventDate(request.getEventDate())
                .eventType(request.getEventType())
                .status(NotificationStatus.PENDING)
                .build();

        return toResponse(notificationRepository.save(notification));
    }

    public NotificationResponse getById(Integer id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));
        return toResponse(n);
    }

    public List<NotificationResponse> getByMotherId(Integer motherId) {
        return notificationRepository.findByMother_MotherIdOrderBySentDateDesc(motherId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadByMotherId(Integer motherId) {
        return notificationRepository.findByMother_MotherIdAndReadAtIsNullOrderBySentDateDesc(motherId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UnreadCountResponse getUnreadCount(Integer motherId) {
        long count = notificationRepository.countByMother_MotherIdAndReadAtIsNull(motherId);
        return UnreadCountResponse.builder().motherId(motherId).unreadCount(count).build();
    }

    public List<NotificationResponse> getByMidwifeId(Integer midwifeId) {
        return notificationRepository.findByMidwife_MidwifeIdOrderBySentDateDesc(midwifeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<NotificationResponse> getByStatus(String status) {
        NotificationStatus ns = NotificationStatus.valueOf(status);
        return notificationRepository.findByStatusOrderBySentDateDesc(ns)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public NotificationResponse markAsRead(Integer id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));
        n.setReadAt(LocalDateTime.now());
        return toResponse(notificationRepository.save(n));
    }

    @Transactional
    public NotificationResponse markAsResponded(Integer id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));
        n.setRespondedAt(LocalDateTime.now());
        return toResponse(notificationRepository.save(n));
    }

    @Transactional
    public NotificationResponse updateStatus(Integer id, String status) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));
        n.setStatus(NotificationStatus.valueOf(status));
        return toResponse(notificationRepository.save(n));
    }

    @Transactional
    public void delete(Integer id) {
        if (!notificationRepository.existsById(id)) {
            throw new IllegalArgumentException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .typeId(n.getNotificationType().getTypeId())
                .typeName(n.getNotificationType().getTypeName())
                .priority(n.getNotificationType().getPriority() != null
                        ? n.getNotificationType().getPriority().name() : null)
                .midwifeId(n.getMidwife() != null ? n.getMidwife().getMidwifeId() : null)
                .midwifeName(n.getMidwife() != null ? n.getMidwife().getName() : null)
                .motherId(n.getMother().getMotherId())
                .motherName(n.getMother().getName())
                .message(n.getMessage())
                .status(n.getStatus() != null ? n.getStatus().name() : null)
                .deliveryMethod(n.getDeliveryMethod() != null ? n.getDeliveryMethod().name() : null)
                .sentDate(n.getSentDate() != null ? n.getSentDate().toString() : null)
                .eventDate(n.getEventDate())
                .eventType(n.getEventType())
                .readAt(n.getReadAt() != null ? n.getReadAt().toString() : null)
                .respondedAt(n.getRespondedAt() != null ? n.getRespondedAt().toString() : null)
                .createdAt(n.getCreatedAt() != null ? n.getCreatedAt().toString() : null)
                .build();
    }
}

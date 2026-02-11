package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.NotificationDtos.NotificationTypeRequest;
import com.moh.moh_backend.dto.NotificationDtos.NotificationTypeResponse;
import com.moh.moh_backend.model.NotificationType;
import com.moh.moh_backend.model.NotificationType.Priority;
import com.moh.moh_backend.repository.NotificationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationTypeService {

    private final NotificationTypeRepository typeRepository;

    @Transactional
    public NotificationTypeResponse create(NotificationTypeRequest request) {
        if (request.getTypeName() == null || request.getTypeName().isBlank()) {
            throw new IllegalArgumentException("Notification type name is required");
        }
        if (typeRepository.findByTypeName(request.getTypeName()).isPresent()) {
            throw new IllegalArgumentException("Notification type already exists: " + request.getTypeName());
        }

        NotificationType entity = NotificationType.builder()
                .typeName(request.getTypeName())
                .description(request.getDescription())
                .template(request.getTemplate())
                .priority(request.getPriority() != null ? Priority.valueOf(request.getPriority()) : null)
                .build();

        return toResponse(typeRepository.save(entity));
    }

    public List<NotificationTypeResponse> getAll() {
        return typeRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public NotificationTypeResponse getById(Integer id) {
        NotificationType entity = typeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification type not found with id: " + id));
        return toResponse(entity);
    }

    @Transactional
    public NotificationTypeResponse update(Integer id, NotificationTypeRequest request) {
        NotificationType entity = typeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification type not found with id: " + id));

        if (request.getTypeName() != null && !request.getTypeName().isBlank()) {
            entity.setTypeName(request.getTypeName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getTemplate() != null) {
            entity.setTemplate(request.getTemplate());
        }
        if (request.getPriority() != null) {
            entity.setPriority(Priority.valueOf(request.getPriority()));
        }

        return toResponse(typeRepository.save(entity));
    }

    @Transactional
    public void delete(Integer id) {
        if (!typeRepository.existsById(id)) {
            throw new IllegalArgumentException("Notification type not found with id: " + id);
        }
        typeRepository.deleteById(id);
    }

    private NotificationTypeResponse toResponse(NotificationType e) {
        return NotificationTypeResponse.builder()
                .typeId(e.getTypeId())
                .typeName(e.getTypeName())
                .description(e.getDescription())
                .template(e.getTemplate())
                .priority(e.getPriority() != null ? e.getPriority().name() : null)
                .build();
    }
}

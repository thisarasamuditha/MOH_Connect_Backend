package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.SessionDtos.SessionTypeRequest;
import com.moh.moh_backend.dto.SessionDtos.SessionTypeResponse;
import com.moh.moh_backend.model.SessionType;
import com.moh.moh_backend.model.SessionType.TargetAudience;
import com.moh.moh_backend.repository.SessionTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionTypeService {

    private final SessionTypeRepository sessionTypeRepository;

    @Transactional
    public SessionTypeResponse create(SessionTypeRequest request) {
        if (request.getTypeName() == null || request.getTypeName().isBlank()) {
            throw new IllegalArgumentException("Session type name is required");
        }
        if (sessionTypeRepository.findByTypeName(request.getTypeName()).isPresent()) {
            throw new IllegalArgumentException("Session type already exists: " + request.getTypeName());
        }

        SessionType entity = SessionType.builder()
                .typeName(request.getTypeName())
                .description(request.getDescription())
                .targetAudience(request.getTargetAudience() != null
                        ? TargetAudience.valueOf(request.getTargetAudience())
                        : null)
                .build();

        return toResponse(sessionTypeRepository.save(entity));
    }

    public List<SessionTypeResponse> getAll() {
        return sessionTypeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SessionTypeResponse getById(Integer id) {
        SessionType entity = sessionTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session type not found with id: " + id));
        return toResponse(entity);
    }

    @Transactional
    public SessionTypeResponse update(Integer id, SessionTypeRequest request) {
        SessionType entity = sessionTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session type not found with id: " + id));

        if (request.getTypeName() != null && !request.getTypeName().isBlank()) {
            entity.setTypeName(request.getTypeName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getTargetAudience() != null) {
            entity.setTargetAudience(TargetAudience.valueOf(request.getTargetAudience()));
        }

        return toResponse(sessionTypeRepository.save(entity));
    }

    @Transactional
    public void delete(Integer id) {
        if (!sessionTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Session type not found with id: " + id);
        }
        sessionTypeRepository.deleteById(id);
    }

    private SessionTypeResponse toResponse(SessionType e) {
        return SessionTypeResponse.builder()
                .sessionTypeId(e.getSessionTypeId())
                .typeName(e.getTypeName())
                .description(e.getDescription())
                .targetAudience(e.getTargetAudience() != null ? e.getTargetAudience().name() : null)
                .build();
    }
}

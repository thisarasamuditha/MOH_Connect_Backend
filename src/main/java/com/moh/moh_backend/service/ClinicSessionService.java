package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.SessionDtos.SessionCreateRequest;
import com.moh.moh_backend.dto.SessionDtos.SessionResponse;
import com.moh.moh_backend.dto.SessionDtos.SessionUpdateRequest;
import com.moh.moh_backend.model.ClinicSession;
import com.moh.moh_backend.model.ClinicSession.SessionStatus;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.model.PhmArea;
import com.moh.moh_backend.model.SessionType;
import com.moh.moh_backend.repository.ClinicSessionRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.PhmAreaRepository;
import com.moh.moh_backend.repository.SessionTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicSessionService {

    private final ClinicSessionRepository sessionRepository;
    private final MidwifeRepository midwifeRepository;
    private final SessionTypeRepository sessionTypeRepository;
    private final PhmAreaRepository phmAreaRepository;

    @Transactional
    public SessionResponse create(SessionCreateRequest request) {
        if (request.getMidwifeId() == null) {
            throw new IllegalArgumentException("Midwife ID is required");
        }
        if (request.getSessionTypeId() == null) {
            throw new IllegalArgumentException("Session type ID is required");
        }
        if (request.getPhmAreaId() == null) {
            throw new IllegalArgumentException("PHM area ID is required");
        }
        if (request.getSessionDate() == null) {
            throw new IllegalArgumentException("Session date is required");
        }
        if (request.getStartTime() == null) {
            throw new IllegalArgumentException("Start time is required");
        }

        Midwife midwife = midwifeRepository.findById(request.getMidwifeId())
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found with id: " + request.getMidwifeId()));

        SessionType sessionType = sessionTypeRepository.findById(request.getSessionTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Session type not found with id: " + request.getSessionTypeId()));

        PhmArea phmArea = phmAreaRepository.findById(request.getPhmAreaId())
                .orElseThrow(() -> new IllegalArgumentException("PHM area not found with id: " + request.getPhmAreaId()));

        ClinicSession session = ClinicSession.builder()
                .midwife(midwife)
                .sessionType(sessionType)
                .phmArea(phmArea)
                .sessionDate(request.getSessionDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .topic(request.getTopic())
                .venue(request.getVenue())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .status(SessionStatus.SCHEDULED)
                .build();

        return toResponse(sessionRepository.save(session));
    }

    public SessionResponse getById(Integer sessionId) {
        ClinicSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found with id: " + sessionId));
        return toResponse(session);
    }

    public List<SessionResponse> getByMidwifeId(Integer midwifeId) {
        return sessionRepository.findByMidwife_MidwifeIdOrderBySessionDateDesc(midwifeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SessionResponse> getByPhmAreaId(Integer phmAreaId) {
        return sessionRepository.findByPhmArea_PhmAreaIdOrderBySessionDateDesc(phmAreaId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SessionResponse> getByStatus(String status) {
        SessionStatus sessionStatus = SessionStatus.valueOf(status);
        return sessionRepository.findByStatusOrderBySessionDateAsc(sessionStatus)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SessionResponse> getByDateRange(LocalDate from, LocalDate to) {
        return sessionRepository.findBySessionDateBetweenOrderBySessionDateAsc(from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SessionResponse> getScheduledByMidwife(Integer midwifeId) {
        return sessionRepository.findByMidwife_MidwifeIdAndStatusOrderBySessionDateAsc(midwifeId, SessionStatus.SCHEDULED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public SessionResponse update(Integer sessionId, SessionUpdateRequest request) {
        ClinicSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found with id: " + sessionId));

        if (request.getSessionDate() != null) session.setSessionDate(request.getSessionDate());
        if (request.getStartTime() != null) session.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) session.setEndTime(request.getEndTime());
        if (request.getTopic() != null) session.setTopic(request.getTopic());
        if (request.getVenue() != null) session.setVenue(request.getVenue());
        if (request.getDescription() != null) session.setDescription(request.getDescription());
        if (request.getCapacity() != null) session.setCapacity(request.getCapacity());
        if (request.getStatus() != null) session.setStatus(SessionStatus.valueOf(request.getStatus()));

        return toResponse(sessionRepository.save(session));
    }

    @Transactional
    public void delete(Integer sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new IllegalArgumentException("Session not found with id: " + sessionId);
        }
        sessionRepository.deleteById(sessionId);
    }

    private SessionResponse toResponse(ClinicSession s) {
        return SessionResponse.builder()
                .sessionId(s.getSessionId())
                .midwifeId(s.getMidwife().getMidwifeId())
                .midwifeName(s.getMidwife().getName())
                .sessionTypeId(s.getSessionType().getSessionTypeId())
                .sessionTypeName(s.getSessionType().getTypeName())
                .phmAreaId(s.getPhmArea().getPhmAreaId())
                .phmAreaName(s.getPhmArea().getAreaName())
                .sessionDate(s.getSessionDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .topic(s.getTopic())
                .venue(s.getVenue())
                .description(s.getDescription())
                .capacity(s.getCapacity())
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .createdAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null)
                .build();
    }
}

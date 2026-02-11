package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.SessionDtos.AttendanceCreateRequest;
import com.moh.moh_backend.dto.SessionDtos.AttendanceMarkRequest;
import com.moh.moh_backend.dto.SessionDtos.AttendanceResponse;
import com.moh.moh_backend.model.ClinicSession;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.SessionAttendance;
import com.moh.moh_backend.repository.ClinicSessionRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.SessionAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionAttendanceService {

    private final SessionAttendanceRepository attendanceRepository;
    private final ClinicSessionRepository sessionRepository;
    private final MotherRepository motherRepository;

    /**
     * Register a mother for a session (pre-attendance).
     */
    @Transactional
    public AttendanceResponse register(AttendanceCreateRequest request) {
        if (request.getSessionId() == null) {
            throw new IllegalArgumentException("Session ID is required");
        }
        if (request.getMotherId() == null) {
            throw new IllegalArgumentException("Mother ID is required");
        }

        ClinicSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found with id: " + request.getSessionId()));

        if (session.getStatus() == ClinicSession.SessionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot register for a cancelled session");
        }

        // Check capacity
        if (session.getCapacity() != null) {
            long currentCount = attendanceRepository.countBySession_SessionIdAndAttendedTrue(session.getSessionId());
            // We count registrations (not just attended) to enforce capacity at registration time
            long totalRegistered = attendanceRepository.findBySession_SessionIdOrderByCreatedAtDesc(session.getSessionId()).size();
            if (totalRegistered >= session.getCapacity()) {
                throw new IllegalStateException("Session is at full capacity (" + session.getCapacity() + ")");
            }
        }

        Mother mother = motherRepository.findById(request.getMotherId())
                .orElseThrow(() -> new IllegalArgumentException("Mother not found with id: " + request.getMotherId()));

        // Prevent duplicate registration
        if (attendanceRepository.findBySession_SessionIdAndMother_MotherId(
                request.getSessionId(), request.getMotherId()).isPresent()) {
            throw new IllegalStateException("Mother is already registered for this session");
        }

        SessionAttendance attendance = SessionAttendance.builder()
                .session(session)
                .mother(mother)
                .attended(false)
                .notes(request.getNotes())
                .build();

        return toResponse(attendanceRepository.save(attendance));
    }

    /**
     * Mark attendance (attended=true/false) and optionally add feedback.
     */
    @Transactional
    public AttendanceResponse markAttendance(Integer attendanceId, AttendanceMarkRequest request) {
        SessionAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found with id: " + attendanceId));

        if (request.getAttended() != null) {
            attendance.setAttended(request.getAttended());
            if (request.getAttended()) {
                attendance.setAttendanceTime(LocalDateTime.now());
            }
        }
        if (request.getFeedback() != null) {
            attendance.setFeedback(request.getFeedback());
        }

        return toResponse(attendanceRepository.save(attendance));
    }

    public List<AttendanceResponse> getBySessionId(Integer sessionId) {
        return attendanceRepository.findBySession_SessionIdOrderByCreatedAtDesc(sessionId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<AttendanceResponse> getByMotherId(Integer motherId) {
        return attendanceRepository.findByMother_MotherIdOrderByCreatedAtDesc(motherId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void delete(Integer attendanceId) {
        if (!attendanceRepository.existsById(attendanceId)) {
            throw new IllegalArgumentException("Attendance record not found with id: " + attendanceId);
        }
        attendanceRepository.deleteById(attendanceId);
    }

    private AttendanceResponse toResponse(SessionAttendance a) {
        return AttendanceResponse.builder()
                .attendanceId(a.getAttendanceId())
                .sessionId(a.getSession().getSessionId())
                .sessionTopic(a.getSession().getTopic())
                .sessionDate(a.getSession().getSessionDate())
                .motherId(a.getMother().getMotherId())
                .motherName(a.getMother().getName())
                .attended(a.getAttended())
                .attendanceTime(a.getAttendanceTime() != null ? a.getAttendanceTime().toString() : null)
                .notes(a.getNotes())
                .feedback(a.getFeedback())
                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : null)
                .build();
    }
}

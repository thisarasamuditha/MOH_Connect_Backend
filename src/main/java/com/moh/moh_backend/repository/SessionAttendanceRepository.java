package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.SessionAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionAttendanceRepository extends JpaRepository<SessionAttendance, Integer> {

    List<SessionAttendance> findBySession_SessionIdOrderByCreatedAtDesc(Integer sessionId);

    List<SessionAttendance> findByMother_MotherIdOrderByCreatedAtDesc(Integer motherId);

    Optional<SessionAttendance> findBySession_SessionIdAndMother_MotherId(Integer sessionId, Integer motherId);

    long countBySession_SessionIdAndAttendedTrue(Integer sessionId);
}

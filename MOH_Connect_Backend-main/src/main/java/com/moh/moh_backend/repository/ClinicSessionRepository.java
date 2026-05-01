package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.ClinicSession;
import com.moh.moh_backend.model.ClinicSession.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ClinicSessionRepository extends JpaRepository<ClinicSession, Integer> {

    List<ClinicSession> findByMidwife_MidwifeIdOrderBySessionDateDesc(Integer midwifeId);

    List<ClinicSession> findByPhmArea_PhmAreaIdOrderBySessionDateDesc(Integer phmAreaId);

    List<ClinicSession> findByStatusOrderBySessionDateAsc(SessionStatus status);

    List<ClinicSession> findBySessionDateBetweenOrderBySessionDateAsc(LocalDate from, LocalDate to);

    List<ClinicSession> findByMidwife_MidwifeIdAndStatusOrderBySessionDateAsc(Integer midwifeId, SessionStatus status);
}

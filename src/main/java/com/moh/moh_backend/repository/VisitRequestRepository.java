package com.moh.moh_backend.repository;

import com.moh.moh_backend.Enum.VisitRequestStatus;
import com.moh.moh_backend.model.VisitRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VisitRequestRepository extends JpaRepository<VisitRequest, Integer> {
    List<VisitRequest> findByMother_MotherIdOrderByCreatedAtDesc(Integer motherId);
    List<VisitRequest> findByPhmArea_PhmAreaIdOrderByCreatedAtDesc(Integer phmAreaId);
    List<VisitRequest> findByPhmArea_PhmAreaIdAndStatusIn(Integer phmAreaId, Collection<VisitRequestStatus> statuses);
}

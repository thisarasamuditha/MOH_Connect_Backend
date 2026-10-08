package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.MotherRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MotherRecordRepository extends JpaRepository<MotherRecord, Integer> {
    /** Visits in chronological order (oldest first), so the last element is the latest visit. */
    List<MotherRecord> findByPregnancy_PregnancyIdOrderByRecordDateAscRecordIdAsc(Integer pregnancyId);

    /** Records for one PHM area, optionally filtered by verification status, newest first. */
    @Query("SELECT r FROM MotherRecord r JOIN FETCH r.pregnancy p JOIN FETCH p.mother m "
            + "LEFT JOIN FETCH r.midwife LEFT JOIN FETCH r.doctor "
            + "WHERE m.phmArea.phmAreaId = :areaId "
            + "AND (:status IS NULL OR r.verificationStatus = :status) "
            + "ORDER BY r.recordDate DESC, r.recordId DESC")
    List<MotherRecord> findForReview(@Param("areaId") Integer areaId, @Param("status") String status);
}

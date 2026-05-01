package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.TargetGroup;
import com.moh.moh_backend.model.VaccineSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaccineScheduleRepository extends JpaRepository<VaccineSchedule, Integer> {
    List<VaccineSchedule> findByTargetGroup(TargetGroup targetGroup);
    List<VaccineSchedule> findByTargetGroupOrderByRecommendedAgeDaysAsc(TargetGroup targetGroup);
}

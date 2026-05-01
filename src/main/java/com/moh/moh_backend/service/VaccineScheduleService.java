package com.moh.moh_backend.service;

import com.moh.moh_backend.model.TargetGroup;
import com.moh.moh_backend.model.VaccineSchedule;
import com.moh.moh_backend.repository.VaccineScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VaccineScheduleService {

    private final VaccineScheduleRepository vaccineScheduleRepository;

    public VaccineScheduleService(VaccineScheduleRepository vaccineScheduleRepository) {
        this.vaccineScheduleRepository = vaccineScheduleRepository;
    }

    @Transactional
    public VaccineSchedule createSchedule(VaccineSchedule schedule) {
        if (schedule.getVaccineName() == null || schedule.getVaccineName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vaccine name is required");
        }
        if (schedule.getTargetGroup() == null) {
            throw new IllegalArgumentException("Target group is required");
        }
        if (schedule.getDoseNumber() == null || schedule.getDoseNumber() < 1) {
            throw new IllegalArgumentException("Valid dose number is required");
        }
        return vaccineScheduleRepository.save(schedule);
    }

    public VaccineSchedule getScheduleById(Integer scheduleId) {
        return vaccineScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Vaccine schedule not found with id: " + scheduleId));
    }

    public List<VaccineSchedule> getAllSchedules() {
        return vaccineScheduleRepository.findAll();
    }

    public List<VaccineSchedule> getSchedulesByTargetGroup(TargetGroup targetGroup) {
        return vaccineScheduleRepository.findByTargetGroupOrderByRecommendedAgeDaysAsc(targetGroup);
    }

    @Transactional
    public VaccineSchedule updateSchedule(Integer scheduleId, VaccineSchedule updatedSchedule) {
        VaccineSchedule existing = getScheduleById(scheduleId);

        if (updatedSchedule.getVaccineName() != null) {
            existing.setVaccineName(updatedSchedule.getVaccineName());
        }
        if (updatedSchedule.getTargetGroup() != null) {
            existing.setTargetGroup(updatedSchedule.getTargetGroup());
        }
        if (updatedSchedule.getDoseNumber() != null) {
            existing.setDoseNumber(updatedSchedule.getDoseNumber());
        }
        if (updatedSchedule.getRecommendedAgeDays() != null) {
            existing.setRecommendedAgeDays(updatedSchedule.getRecommendedAgeDays());
        }
        if (updatedSchedule.getDescription() != null) {
            existing.setDescription(updatedSchedule.getDescription());
        }

        return vaccineScheduleRepository.save(existing);
    }

    @Transactional
    public void deleteSchedule(Integer scheduleId) {
        if (!vaccineScheduleRepository.existsById(scheduleId)) {
            throw new RuntimeException("Vaccine schedule not found with id: " + scheduleId);
        }
        vaccineScheduleRepository.deleteById(scheduleId);
    }
}

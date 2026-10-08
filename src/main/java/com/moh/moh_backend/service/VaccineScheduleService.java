package com.moh.moh_backend.service;

import com.moh.moh_backend.model.TargetGroup;
import com.moh.moh_backend.model.VaccineSchedule;
import com.moh.moh_backend.repository.VaccineScheduleRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @CacheEvict(value = "vaccineSchedules", allEntries = true)
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

    /** Active schedule rows only (the current national schedule). */
    @Cacheable("vaccineSchedules")
    public List<VaccineSchedule> getAllSchedules() {
        return vaccineScheduleRepository.findAll().stream()
                .filter(VaccineSchedule::isCurrent)
                .sorted(java.util.Comparator
                        .comparing((VaccineSchedule v) -> v.getRecommendedAgeDays() == null ? Integer.MAX_VALUE : v.getRecommendedAgeDays())
                        .thenComparing(VaccineSchedule::getVaccineName))
                .toList();
    }

    /** Includes deactivated rows, for administration and history. */
    public List<VaccineSchedule> getAllSchedulesIncludingInactive() {
        return vaccineScheduleRepository.findAll();
    }

    @Cacheable("vaccineSchedules")
    public List<VaccineSchedule> getSchedulesByTargetGroup(TargetGroup targetGroup) {
        return vaccineScheduleRepository.findByTargetGroupOrderByRecommendedAgeDaysAsc(targetGroup).stream()
                .filter(VaccineSchedule::isCurrent)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "vaccineSchedules", allEntries = true)
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
        if (updatedSchedule.getIsActive() != null) {
            existing.setIsActive(updatedSchedule.getIsActive());
        }
        if (updatedSchedule.getIsConditional() != null) {
            existing.setIsConditional(updatedSchedule.getIsConditional());
        }
        if (updatedSchedule.getTargetGender() != null) {
            existing.setTargetGender(updatedSchedule.getTargetGender().isBlank() ? null : updatedSchedule.getTargetGender().trim().toUpperCase());
        }

        return vaccineScheduleRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "vaccineSchedules", allEntries = true)
    public void deleteSchedule(Integer scheduleId) {
        if (!vaccineScheduleRepository.existsById(scheduleId)) {
            throw new RuntimeException("Vaccine schedule not found with id: " + scheduleId);
        }
        vaccineScheduleRepository.deleteById(scheduleId);
    }
}

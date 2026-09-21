package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.BabyVaccinationCreateDto;
import com.moh.moh_backend.dto.BabyVaccinationResponseDto;
import com.moh.moh_backend.model.*;
import com.moh.moh_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BabyVaccinationService {

    private final BabyVaccinationRepository babyVaccinationRepository;
    private final BabyRepository babyRepository;
    private final MidwifeRepository midwifeRepository;
    private final VaccineScheduleRepository vaccineScheduleRepository;
    private final MotherRepository motherRepository;
    private final MidwifeRepository midwifeAccessRepository;

    public BabyVaccinationService(BabyVaccinationRepository babyVaccinationRepository,
                                  BabyRepository babyRepository,
                                  MidwifeRepository midwifeRepository,
                                  VaccineScheduleRepository vaccineScheduleRepository,
                                  MotherRepository motherRepository) {
        this.babyVaccinationRepository = babyVaccinationRepository;
        this.babyRepository = babyRepository;
        this.midwifeRepository = midwifeRepository;
        this.vaccineScheduleRepository = vaccineScheduleRepository;
        this.motherRepository = motherRepository;
        this.midwifeAccessRepository = midwifeRepository;
    }

    @Transactional
    public BabyVaccinationResponseDto administerVaccine(BabyVaccinationCreateDto dto,
                                                         Integer userId, String role) {
        // Validate baby
        Baby baby = babyRepository.findById(dto.getBabyId())
                .orElseThrow(() -> new RuntimeException("Baby not found with id: " + dto.getBabyId()));
        assertCanAccessBaby(baby, userId, role);

        // Validate vaccine schedule
        VaccineSchedule schedule = vaccineScheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Vaccine schedule not found with id: " + dto.getScheduleId()));

        // Verify schedule is for babies
        if (schedule.getTargetGroup() != TargetGroup.BABY) {
            throw new IllegalArgumentException("Selected vaccine schedule is not for babies");
        }

        // Validate vaccination date
        if (dto.getVaccinationDate() == null) {
            throw new IllegalArgumentException("Vaccination date is required");
        }

        // Create vaccination entity
        BabyVaccination vaccination = new BabyVaccination();
        vaccination.setBaby(baby);
        vaccination.setSchedule(schedule);
        vaccination.setVaccinationDate(dto.getVaccinationDate());
        vaccination.setBatchNumber(dto.getBatchNumber());
        vaccination.setManufacturer(dto.getManufacturer());
        vaccination.setNextDoseDate(dto.getNextDoseDate());
        vaccination.setAdverseReaction(dto.getAdverseReaction());

        // Set midwife if provided
        if (dto.getMidwifeId() != null) {
            Midwife midwife = midwifeRepository.findById(dto.getMidwifeId())
                    .orElseThrow(() -> new RuntimeException("Midwife not found with id: " + dto.getMidwifeId()));
            vaccination.setMidwife(midwife);
        }

        BabyVaccination saved = babyVaccinationRepository.save(vaccination);
        return mapToResponseDto(saved);
    }

    public BabyVaccinationResponseDto getVaccinationById(Integer vaccinationId, Integer userId, String role) {
        BabyVaccination vaccination = babyVaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new RuntimeException("Baby vaccination not found with id: " + vaccinationId));
            assertCanAccessBaby(vaccination.getBaby(), userId, role);
        return mapToResponseDto(vaccination);
    }

    public List<BabyVaccinationResponseDto> getVaccinationsByBaby(Integer babyId, Integer userId, String role) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new RuntimeException("Baby not found with id: " + babyId));
        assertCanAccessBaby(baby, userId, role);
        return babyVaccinationRepository.findByBabyIdOrderByVaccinationDateDesc(babyId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<BabyVaccinationResponseDto> getVaccinationsByMother(Integer motherId, Integer userId, String role) {
        var mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new RuntimeException("Mother not found with id: " + motherId));
        assertCanAccessMother(mother, userId, role);
        return babyVaccinationRepository.findByMotherId(motherId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVaccination(Integer vaccinationId, Integer userId, String role) {
        BabyVaccination vaccination = babyVaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new RuntimeException("Baby vaccination not found with id: " + vaccinationId));
        assertCanAccessBaby(vaccination.getBaby(), userId, role);
        babyVaccinationRepository.delete(vaccination);
    }

    private void assertCanAccessBaby(Baby baby, Integer userId, String role) {
        var mother = motherRepository.findById(baby.getMotherId())
                .orElseThrow(() -> new IllegalStateException("Mother not found for child"));
        assertCanAccessMother(mother, userId, role);
    }

    private void assertCanAccessMother(Mother mother, Integer userId, String role) {
        if ("MOTHER".equalsIgnoreCase(role)) {
            if (mother.getUser() == null || !userId.equals(mother.getUser().getUserId())) {
                throw new IllegalStateException("Mothers can only access their own vaccination records");
            }
        } else if ("MIDWIFE".equalsIgnoreCase(role)) {
            Integer areaId = midwifeAccessRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Midwife not found"))
                    .getPhmArea().getPhmAreaId();
            if (mother.getPhmArea() == null || !areaId.equals(mother.getPhmArea().getPhmAreaId())) {
                throw new IllegalStateException("Midwives can only access vaccination records in their PHM area");
            }
        }
    }

    private BabyVaccinationResponseDto mapToResponseDto(BabyVaccination vaccination) {
        BabyVaccinationResponseDto dto = new BabyVaccinationResponseDto();
        dto.setVaccinationId(vaccination.getVaccinationId());
        dto.setBabyId(vaccination.getBaby().getBabyId());
        dto.setBabyName(vaccination.getBaby().getName());
        
        if (vaccination.getMidwife() != null) {
            dto.setMidwifeId(vaccination.getMidwife().getMidwifeId());
            dto.setMidwifeName(vaccination.getMidwife().getName());
        }
        
        dto.setSchedule(vaccination.getSchedule());
        dto.setVaccinationDate(vaccination.getVaccinationDate());
        dto.setBatchNumber(vaccination.getBatchNumber());
        dto.setManufacturer(vaccination.getManufacturer());
        dto.setNextDoseDate(vaccination.getNextDoseDate());
        dto.setAdverseReaction(vaccination.getAdverseReaction());
        dto.setCreatedAt(vaccination.getCreatedAt());
        
        return dto;
    }
}

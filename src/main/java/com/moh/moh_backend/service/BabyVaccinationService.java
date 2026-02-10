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

    public BabyVaccinationService(BabyVaccinationRepository babyVaccinationRepository,
                                  BabyRepository babyRepository,
                                  MidwifeRepository midwifeRepository,
                                  VaccineScheduleRepository vaccineScheduleRepository) {
        this.babyVaccinationRepository = babyVaccinationRepository;
        this.babyRepository = babyRepository;
        this.midwifeRepository = midwifeRepository;
        this.vaccineScheduleRepository = vaccineScheduleRepository;
    }

    @Transactional
    public BabyVaccinationResponseDto administerVaccine(BabyVaccinationCreateDto dto) {
        // Validate baby
        Baby baby = babyRepository.findById(dto.getBabyId())
                .orElseThrow(() -> new RuntimeException("Baby not found with id: " + dto.getBabyId()));

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

    public BabyVaccinationResponseDto getVaccinationById(Integer vaccinationId) {
        BabyVaccination vaccination = babyVaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new RuntimeException("Baby vaccination not found with id: " + vaccinationId));
        return mapToResponseDto(vaccination);
    }

    public List<BabyVaccinationResponseDto> getVaccinationsByBaby(Integer babyId) {
        return babyVaccinationRepository.findByBabyIdOrderByVaccinationDateDesc(babyId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<BabyVaccinationResponseDto> getVaccinationsByMother(Integer motherId) {
        return babyVaccinationRepository.findByMotherId(motherId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVaccination(Integer vaccinationId) {
        if (!babyVaccinationRepository.existsById(vaccinationId)) {
            throw new RuntimeException("Baby vaccination not found with id: " + vaccinationId);
        }
        babyVaccinationRepository.deleteById(vaccinationId);
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

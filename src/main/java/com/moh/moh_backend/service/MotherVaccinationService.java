package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.MotherVaccinationCreateDto;
import com.moh.moh_backend.dto.MotherVaccinationResponseDto;
import com.moh.moh_backend.model.*;
import com.moh.moh_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MotherVaccinationService {

    private final MotherVaccinationRepository motherVaccinationRepository;
    private final PregnancyRepository pregnancyRepository;
    private final MidwifeRepository midwifeRepository;
    private final VaccineScheduleRepository vaccineScheduleRepository;
    private final PregnancyService pregnancyService;

    public MotherVaccinationService(MotherVaccinationRepository motherVaccinationRepository,
                                    PregnancyRepository pregnancyRepository,
                                    MidwifeRepository midwifeRepository,
                                    VaccineScheduleRepository vaccineScheduleRepository,
                                    PregnancyService pregnancyService) {
        this.motherVaccinationRepository = motherVaccinationRepository;
        this.pregnancyRepository = pregnancyRepository;
        this.midwifeRepository = midwifeRepository;
        this.vaccineScheduleRepository = vaccineScheduleRepository;
        this.pregnancyService = pregnancyService;
    }

    @Transactional
    public MotherVaccinationResponseDto administerVaccine(MotherVaccinationCreateDto dto,
                                                           Integer userId, String role) {
        // Validate pregnancy
        Pregnancy pregnancy = pregnancyRepository.findById(dto.getPregnancyId())
                .orElseThrow(() -> new RuntimeException("Pregnancy not found with id: " + dto.getPregnancyId()));
        pregnancyService.assertCanAccessPregnancy(dto.getPregnancyId(), userId, role);

        // Validate vaccine schedule
        VaccineSchedule schedule = vaccineScheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Vaccine schedule not found with id: " + dto.getScheduleId()));

        // Verify schedule is for mothers
        if (schedule.getTargetGroup() != TargetGroup.MOTHER) {
            throw new IllegalArgumentException("Selected vaccine schedule is not for mothers");
        }

        // Validate vaccination date
        if (dto.getVaccinationDate() == null) {
            throw new IllegalArgumentException("Vaccination date is required");
        }

        // Create vaccination entity
        MotherVaccination vaccination = new MotherVaccination();
        vaccination.setPregnancy(pregnancy);
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

        MotherVaccination saved = motherVaccinationRepository.save(vaccination);
        return mapToResponseDto(saved);
    }

    public MotherVaccinationResponseDto getVaccinationById(Integer vaccinationId, Integer userId, String role) {
        MotherVaccination vaccination = motherVaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new RuntimeException("Mother vaccination not found with id: " + vaccinationId));
            pregnancyService.assertCanAccessPregnancy(vaccination.getPregnancy().getPregnancyId(), userId, role);
        return mapToResponseDto(vaccination);
    }

    public List<MotherVaccinationResponseDto> getVaccinationsByPregnancy(Integer pregnancyId, Integer userId, String role) {
        pregnancyService.assertCanAccessPregnancy(pregnancyId, userId, role);
        return motherVaccinationRepository.findByPregnancyIdOrderByVaccinationDateDesc(pregnancyId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<MotherVaccinationResponseDto> getVaccinationsByMother(Integer motherId, Integer userId, String role) {
        var pregnancy = pregnancyRepository.findByMother_MotherId(motherId).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Mother has no pregnancy record"));
        pregnancyService.assertCanAccessPregnancy(pregnancy.getPregnancyId(), userId, role);
        return motherVaccinationRepository.findByMotherId(motherId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVaccination(Integer vaccinationId, Integer userId, String role) {
        MotherVaccination vaccination = motherVaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new RuntimeException("Mother vaccination not found with id: " + vaccinationId));
        pregnancyService.assertCanAccessPregnancy(vaccination.getPregnancy().getPregnancyId(), userId, role);
        motherVaccinationRepository.delete(vaccination);
    }

    private MotherVaccinationResponseDto mapToResponseDto(MotherVaccination vaccination) {
        MotherVaccinationResponseDto dto = new MotherVaccinationResponseDto();
        dto.setVaccinationId(vaccination.getVaccinationId());
        dto.setPregnancyId(vaccination.getPregnancy().getPregnancyId());
        dto.setMotherName(vaccination.getPregnancy().getMother().getName());
        
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

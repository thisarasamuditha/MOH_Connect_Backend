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

    public MotherVaccinationService(MotherVaccinationRepository motherVaccinationRepository,
                                    PregnancyRepository pregnancyRepository,
                                    MidwifeRepository midwifeRepository,
                                    VaccineScheduleRepository vaccineScheduleRepository) {
        this.motherVaccinationRepository = motherVaccinationRepository;
        this.pregnancyRepository = pregnancyRepository;
        this.midwifeRepository = midwifeRepository;
        this.vaccineScheduleRepository = vaccineScheduleRepository;
    }

    @Transactional
    public MotherVaccinationResponseDto administerVaccine(MotherVaccinationCreateDto dto) {
        // Validate pregnancy
        Pregnancy pregnancy = pregnancyRepository.findById(dto.getPregnancyId())
                .orElseThrow(() -> new RuntimeException("Pregnancy not found with id: " + dto.getPregnancyId()));

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

    public MotherVaccinationResponseDto getVaccinationById(Integer vaccinationId) {
        MotherVaccination vaccination = motherVaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new RuntimeException("Mother vaccination not found with id: " + vaccinationId));
        return mapToResponseDto(vaccination);
    }

    public List<MotherVaccinationResponseDto> getVaccinationsByPregnancy(Integer pregnancyId) {
        return motherVaccinationRepository.findByPregnancyIdOrderByVaccinationDateDesc(pregnancyId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<MotherVaccinationResponseDto> getVaccinationsByMother(Integer motherId) {
        return motherVaccinationRepository.findByMotherId(motherId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVaccination(Integer vaccinationId) {
        if (!motherVaccinationRepository.existsById(vaccinationId)) {
            throw new RuntimeException("Mother vaccination not found with id: " + vaccinationId);
        }
        motherVaccinationRepository.deleteById(vaccinationId);
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

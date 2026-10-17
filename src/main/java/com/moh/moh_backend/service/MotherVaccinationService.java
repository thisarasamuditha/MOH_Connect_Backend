package com.moh.moh_backend.service;

import com.moh.moh_backend.Enum.TargetGroup;
import com.moh.moh_backend.dto.MotherVaccinationCreateDto;
import com.moh.moh_backend.dto.MotherVaccinationResponseDto;
import com.moh.moh_backend.model.*;
import com.moh.moh_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
        if (dto == null || dto.getPregnancyId() == null) {
            throw new IllegalArgumentException("Pregnancy is required");
        }
        if (dto.getScheduleId() == null) {
            throw new IllegalArgumentException("Select the vaccine that was given");
        }
        Pregnancy pregnancy = pregnancyRepository.findById(dto.getPregnancyId())
                .orElseThrow(() -> new IllegalArgumentException("Pregnancy not found with id: " + dto.getPregnancyId()));
        pregnancyService.assertCanAccessPregnancy(dto.getPregnancyId(), userId, role);

        VaccineSchedule schedule = vaccineScheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Vaccine schedule not found with id: " + dto.getScheduleId()));
        if (schedule.getTargetGroup() != TargetGroup.MOTHER) {
            throw new IllegalArgumentException("Selected vaccine schedule is not for mothers");
        }
        if (!schedule.isCurrent()) {
            throw new IllegalArgumentException("This vaccine schedule entry is no longer in use; choose a current one");
        }

        List<MotherVaccination> existing = motherVaccinationRepository.findByMotherId(pregnancy.getMother().getMotherId());
        validateDose(dto.getVaccinationDate(), schedule, pregnancy, existing, LocalDate.now());

        MotherVaccination vaccination = new MotherVaccination();
        vaccination.setPregnancy(pregnancy);
        vaccination.setSchedule(schedule);
        vaccination.setVaccinationDate(dto.getVaccinationDate());
        vaccination.setBatchNumber(blankToNull(dto.getBatchNumber()));
        vaccination.setManufacturer(blankToNull(dto.getManufacturer()));
        vaccination.setNextDoseDate(dto.getNextDoseDate());
        vaccination.setAdverseReaction(blankToNull(dto.getAdverseReaction()));

        // A midwife records her own doses; others may name the midwife who gave it
        if ("MIDWIFE".equalsIgnoreCase(role)) {
            midwifeRepository.findByUser_UserId(userId).ifPresent(vaccination::setMidwife);
        } else if (dto.getMidwifeId() != null) {
            Midwife midwife = midwifeRepository.findById(dto.getMidwifeId())
                    .orElseThrow(() -> new IllegalArgumentException("Midwife not found with id: " + dto.getMidwifeId()));
            vaccination.setMidwife(midwife);
        }

        MotherVaccination saved = motherVaccinationRepository.save(vaccination);
        return mapToResponseDto(saved);
    }

    /**
     * Rules for recording a maternal vaccine: date given, not in the future, within the pregnancy
     * (from LMP to 6 weeks after its outcome), not the same schedule entry twice in one pregnancy;
     * tetanus doses at least 4 weeks apart and no more than the full course of 5.
     */
    static void validateDose(LocalDate date, VaccineSchedule schedule, Pregnancy pregnancy,
                             List<MotherVaccination> motherVaccinations, LocalDate today) {
        if (date == null) {
            throw new IllegalArgumentException("Vaccination date is required");
        }
        if (date.isAfter(today)) {
            throw new IllegalArgumentException("Vaccination date cannot be in the future");
        }
        if (pregnancy.getLmpDate() != null && date.isBefore(pregnancy.getLmpDate())) {
            throw new IllegalArgumentException("Vaccination date cannot be before the LMP date (" + pregnancy.getLmpDate()
                    + "). Record doses from before this pregnancy as earlier tetanus history.");
        }
        LocalDate outcome = pregnancy.getOutcomeDate() != null ? pregnancy.getOutcomeDate() : pregnancy.getDeliveryDate();
        if (outcome != null && date.isAfter(outcome.plusWeeks(6))) {
            throw new IllegalArgumentException("Vaccination date is more than 6 weeks after this pregnancy ended");
        }

        List<MotherVaccination> all = motherVaccinations == null ? List.of() : motherVaccinations;
        for (MotherVaccination v : all) {
            boolean samePregnancy = v.getPregnancy() != null
                    && pregnancy.getPregnancyId() != null
                    && pregnancy.getPregnancyId().equals(v.getPregnancy().getPregnancyId());
            if (samePregnancy && v.getSchedule() != null
                    && schedule.getScheduleId() != null
                    && schedule.getScheduleId().equals(v.getSchedule().getScheduleId())) {
                throw new IllegalArgumentException(schedule.getVaccineName() + " dose " + schedule.getDoseNumber()
                        + " is already recorded for this pregnancy (" + v.getVaccinationDate() + ")");
            }
        }

        if (MaternalVaccinationDueService.isTetanus(schedule)) {
            Mother mother = pregnancy.getMother();
            int prior = mother != null && mother.getPriorTtDoses() != null ? mother.getPriorTtDoses() : 0;
            List<MotherVaccination> tetanus = all.stream()
                    .filter(v -> MaternalVaccinationDueService.isTetanus(v.getSchedule()))
                    .toList();
            if (prior == MaternalVaccinationDueService.FULL_COURSE_DOSES) {
                throw new IllegalArgumentException("This mother already has " + MaternalVaccinationDueService.FULL_COURSE_DOSES
                        + " tetanus doses and is protected for all childbearing years; no further dose is needed");
            }
            if (prior == MaternalVaccinationDueService.CHILDHOOD_SERIES_DOSES) {
                List<MotherVaccination> thisPregnancyTetanus = tetanus.stream()
                        .filter(v -> v.getPregnancy() != null && pregnancy.getPregnancyId() != null
                                && pregnancy.getPregnancyId().equals(v.getPregnancy().getPregnancyId()))
                        .toList();
                if (!thisPregnancyTetanus.isEmpty()) {
                    throw new IllegalArgumentException("This mother has completed her 6 childhood doses and has already received her 1 booster dose for this pregnancy");
                }
            } else if (prior + tetanus.size() >= MaternalVaccinationDueService.FULL_COURSE_DOSES) {
                throw new IllegalArgumentException("This mother already has " + MaternalVaccinationDueService.FULL_COURSE_DOSES
                        + " tetanus doses and is protected for all childbearing years; no further dose is needed");
            }
            for (MotherVaccination v : tetanus) {
                if (v.getVaccinationDate() != null
                        && Math.abs(ChronoUnit.DAYS.between(v.getVaccinationDate(), date)) < MaternalVaccinationDueService.MIN_INTERVAL_DAYS) {
                    throw new IllegalArgumentException("Tetanus doses must be at least 4 weeks apart; the previous dose was on "
                            + v.getVaccinationDate());
                }
            }
            if (mother != null && mother.getPriorTtLastDate() != null
                    && Math.abs(ChronoUnit.DAYS.between(mother.getPriorTtLastDate(), date)) < MaternalVaccinationDueService.MIN_INTERVAL_DAYS) {
                throw new IllegalArgumentException("Tetanus doses must be at least 4 weeks apart; the earlier recorded dose was on "
                        + mother.getPriorTtLastDate());
            }
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
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
        var pregnancyOpt = pregnancyRepository.findByMother_MotherId(motherId).stream().findFirst();
        if (pregnancyOpt.isEmpty()) {
            return List.of();
        }
        var pregnancy = pregnancyOpt.get();
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

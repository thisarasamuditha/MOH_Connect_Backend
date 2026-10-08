package com.moh.moh_backend.service;

import com.moh.moh_backend.model.GrowthStatus;
import com.moh.moh_backend.util.ClinicalValidation;
import com.moh.moh_backend.util.WhoWeightForAge;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.model.BabyRecord;
import com.moh.moh_backend.model.Doctor;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.repository.BabyRecordRepository;
import com.moh.moh_backend.repository.BabyRepository;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BabyRecordService {

    private final BabyRecordRepository babyRecordRepository;
    private final BabyRepository babyRepository;
    private final MidwifeRepository midwifeRepository;
    private final DoctorRepository doctorRepository;
    private final MotherRepository motherRepository;

    public BabyRecordService(BabyRecordRepository babyRecordRepository,
                             BabyRepository babyRepository,
                             MidwifeRepository midwifeRepository,
                             DoctorRepository doctorRepository,
                             MotherRepository motherRepository) {
        this.babyRecordRepository = babyRecordRepository;
        this.babyRepository = babyRepository;
        this.midwifeRepository = midwifeRepository;
        this.doctorRepository = doctorRepository;
        this.motherRepository = motherRepository;
    }

    @Transactional
    public BabyRecord createBabyRecord(BabyRecord babyRecord, Integer babyId, 
                                       Integer midwifeId, Integer doctorId,
                                       Integer userId, String role) {
        // Validate and set baby (required)
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new RuntimeException("Baby not found with id: " + babyId));
        assertCanAccessBaby(baby, userId, role);
        babyRecord.setBaby(baby);

        // Validate and set midwife (optional)
        if (midwifeId != null) {
            Midwife midwife = midwifeRepository.findById(midwifeId)
                    .orElseThrow(() -> new RuntimeException("Midwife not found with id: " + midwifeId));
            babyRecord.setMidwife(midwife);
        } else if ("MIDWIFE".equalsIgnoreCase(role) && userId != null) {
            midwifeRepository.findByUser_UserId(userId).ifPresent(babyRecord::setMidwife);
        }

        // Validate and set doctor (optional)
        if (doctorId != null) {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
            babyRecord.setDoctor(doctor);
        }

        validateRecordValues(babyRecord, baby);
        WhoWeightForAge.Zone zone = applyWeightForAge(babyRecord, baby);

        // Auto-flag high risks into baby entity if severe condition detected
        boolean isHighRisk = false;
        String riskDetail = "";
        if (zone == WhoWeightForAge.Zone.SEVERE_UNDERWEIGHT || zone == WhoWeightForAge.Zone.MODERATE_UNDERWEIGHT) {
            isHighRisk = true;
            riskDetail = "Growth: " + WhoWeightForAge.label(zone) + ", WFA z=" + babyRecord.getWeightForAgeZ();
        } else if (babyRecord.getGrowthStatus() == com.moh.moh_backend.model.GrowthStatus.WASTED) {
            isHighRisk = true;
            riskDetail = "Growth: WASTED";
        }
        if (babyRecord.getTemperature() != null) {
            try {
                double temp = Double.parseDouble(babyRecord.getTemperature().trim());
                if (temp >= 38.0) {
                    isHighRisk = true;
                    riskDetail = (riskDetail.isEmpty() ? "" : riskDetail + " & ") + String.format("High fever (%.1f°C)", temp);
                }
            } catch (Exception ignored) {}
        }
        if (isHighRisk) {
            String note = "HIGH RISK: " + riskDetail + (babyRecord.getFindings() != null ? " - " + babyRecord.getFindings() : "");
            if (note.length() > 250) note = note.substring(0, 247) + "...";
            baby.setSpecialNotes(note);
            babyRepository.save(baby);
        }

        return babyRecordRepository.save(babyRecord);
    }

    public BabyRecord getBabyRecordById(Integer id, Integer userId, String role) {
        BabyRecord record = babyRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Baby record not found with id: " + id));
        assertCanAccessBaby(record.getBaby(), userId, role);
        return record;
    }

    public List<BabyRecord> getBabyRecordsByBabyId(Integer babyId, Integer userId, String role) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new RuntimeException("Baby not found with id: " + babyId));
        assertCanAccessBaby(baby, userId, role);
        return babyRecordRepository.findByBaby_BabyIdOrderByRecordDateDesc(babyId);
    }

    @Transactional
    public BabyRecord updateBabyRecord(Integer id, BabyRecord updatedRecord, 
                                       Integer midwifeId, Integer doctorId,
                                       Integer userId, String role) {
        BabyRecord existing = getBabyRecordById(id, userId, role);

        // Update fields
        if (updatedRecord.getRecordDate() != null) {
            existing.setRecordDate(updatedRecord.getRecordDate());
        }
        if (updatedRecord.getAgeMonths() != null) {
            existing.setAgeMonths(updatedRecord.getAgeMonths());
        }
        if (updatedRecord.getWeight() != null) {
            existing.setWeight(updatedRecord.getWeight());
        }
        if (updatedRecord.getHeight() != null) {
            existing.setHeight(updatedRecord.getHeight());
        }
        if (updatedRecord.getHeadCircumference() != null) {
            existing.setHeadCircumference(updatedRecord.getHeadCircumference());
        }
        if (updatedRecord.getBmi() != null) {
            existing.setBmi(updatedRecord.getBmi());
        }
        if (updatedRecord.getDevelopmentalMilestones() != null) {
            existing.setDevelopmentalMilestones(updatedRecord.getDevelopmentalMilestones());
        }
        if (updatedRecord.getGrowthStatus() != null) {
            existing.setGrowthStatus(updatedRecord.getGrowthStatus());
        }
        if (updatedRecord.getFindings() != null) {
            existing.setFindings(updatedRecord.getFindings());
        }
        if (updatedRecord.getRecommendations() != null) {
            existing.setRecommendations(updatedRecord.getRecommendations());
        }
        if (updatedRecord.getHealthStatus() != null) {
            existing.setHealthStatus(updatedRecord.getHealthStatus());
        }
        if (updatedRecord.getNotes() != null) {
            existing.setNotes(updatedRecord.getNotes());
        }
        if (updatedRecord.getSkinColor() != null) {
            existing.setSkinColor(updatedRecord.getSkinColor());
        }
        if (updatedRecord.getEyeColor() != null) {
            existing.setEyeColor(updatedRecord.getEyeColor());
        }
        if (updatedRecord.getUmbilicalCordStatus() != null) {
            existing.setUmbilicalCordStatus(updatedRecord.getUmbilicalCordStatus());
        }
        if (updatedRecord.getTemperature() != null) {
            existing.setTemperature(updatedRecord.getTemperature());
        }
        if (updatedRecord.getBreastfeedingStatus() != null) {
            existing.setBreastfeedingStatus(updatedRecord.getBreastfeedingStatus());
        }
        if (updatedRecord.getSessionTime() != null) {
            existing.setSessionTime(updatedRecord.getSessionTime());
        }
        if (updatedRecord.getOtherConditions() != null) {
            existing.setOtherConditions(updatedRecord.getOtherConditions());
        }
        if (updatedRecord.getNextVisitDate() != null) {
            existing.setNextVisitDate(updatedRecord.getNextVisitDate());
        }

        // Update midwife if provided
        if (midwifeId != null) {
            Midwife midwife = midwifeRepository.findById(midwifeId)
                    .orElseThrow(() -> new RuntimeException("Midwife not found with id: " + midwifeId));
            existing.setMidwife(midwife);
        }

        // Update doctor if provided
        if (doctorId != null) {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
            existing.setDoctor(doctor);
        }

        validateRecordValues(existing, existing.getBaby());
        applyWeightForAge(existing, existing.getBaby());
        return babyRecordRepository.save(existing);
    }

    public List<BabyRecord> getBabyRecordsByMotherId(Integer motherId, Integer userId, String role) {
        var mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new RuntimeException("Mother not found with id: " + motherId));
        if ("MOTHER".equalsIgnoreCase(role)) {
            if (mother.getUser() == null || !userId.equals(mother.getUser().getUserId())) {
                throw new IllegalStateException("Mothers can only access their own children's records");
            }
        } else if ("MIDWIFE".equalsIgnoreCase(role)) {
            Integer areaId = midwifeRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Midwife not found"))
                    .getPhmArea().getPhmAreaId();
            if (mother.getPhmArea() == null || !areaId.equals(mother.getPhmArea().getPhmAreaId())) {
                throw new IllegalStateException("Midwives can only access children in their PHM area");
            }
        }
        List<Baby> babies = babyRepository.findByMotherId(motherId);
        if (babies.isEmpty()) {
            return List.of();
        }
        List<Integer> babyIds = babies.stream().map(Baby::getBabyId).toList();
        return babyRecordRepository.findByBaby_BabyIdInOrderByRecordDateDesc(babyIds);
    }

    @Transactional
    public void deleteBabyRecord(Integer id, Integer userId, String role) {
        BabyRecord existing = getBabyRecordById(id, userId, role);
        babyRecordRepository.delete(existing);
    }

    private void assertCanAccessBaby(Baby baby, Integer userId, String role) {
        if (baby == null || baby.getMotherId() == null) {
            throw new IllegalStateException("Unable to verify child ownership");
        }
        var mother = motherRepository.findById(baby.getMotherId())
                .orElseThrow(() -> new IllegalStateException("Mother not found for child"));
        if ("MOTHER".equalsIgnoreCase(role)) {
            if (mother.getUser() == null || !userId.equals(mother.getUser().getUserId())) {
                throw new IllegalStateException("Mothers can only access their own children");
            }
        } else if ("MIDWIFE".equalsIgnoreCase(role)) {
            Integer areaId = midwifeRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Midwife not found"))
                    .getPhmArea().getPhmAreaId();
            if (mother.getPhmArea() == null || !areaId.equals(mother.getPhmArea().getPhmAreaId())) {
                throw new IllegalStateException("Midwives can only access children in their PHM area");
            }
        }
    }

    /**
     * Sets age in months and the weight-for-age growth status (sex-specific WHO z-score, CHDR zones).
     * A status entered as STUNTED or WASTED is kept, because those are length-based assessments
     * that weight-for-age cannot determine.
     */
    private WhoWeightForAge.Zone applyWeightForAge(BabyRecord record, Baby baby) {
        if (baby == null) return null;
        Integer ageDays = null;
        if (baby.getDateOfBirth() != null && record.getRecordDate() != null) {
            ageDays = (int) java.time.temporal.ChronoUnit.DAYS.between(baby.getDateOfBirth(), record.getRecordDate());
            record.setAgeMonths((int) Math.floor(ageDays / WhoWeightForAge.DAYS_PER_MONTH));
        } else if (record.getAgeMonths() != null) {
            ageDays = (int) Math.round(record.getAgeMonths() * WhoWeightForAge.DAYS_PER_MONTH);
        }
        if (record.getWeight() == null || ageDays == null || baby.getGender() == null) {
            return null;
        }
        Double z = WhoWeightForAge.zScore(baby.getGender(), ageDays, record.getWeight());
        WhoWeightForAge.Zone zone = WhoWeightForAge.zone(z);
        GrowthStatus current = record.getGrowthStatus();
        if (zone != null && current != GrowthStatus.STUNTED && current != GrowthStatus.WASTED) {
            record.setGrowthStatus(WhoWeightForAge.toGrowthStatus(zone));
        }
        return zone;
    }

    private void validateRecordValues(BabyRecord r, Baby baby) {
        if (r.getRecordDate() == null) {
            throw new IllegalArgumentException("Visit date is required");
        }
        ClinicalValidation.notInFuture(r.getRecordDate(), "Visit date");
        if (baby != null) {
            ClinicalValidation.notBefore(r.getRecordDate(), baby.getDateOfBirth(), "Visit date", "the date of birth");
        }
        ClinicalValidation.range(r.getWeight(), 0.5, 40, "Weight (kg)");
        ClinicalValidation.range(r.getHeight(), 30, 130, "Length/height (cm)");
        ClinicalValidation.range(r.getHeadCircumference(), 25, 60, "Head circumference (cm)");
        ClinicalValidation.temperature(r.getTemperature());
        if (r.getNextVisitDate() != null && !r.getNextVisitDate().isAfter(r.getRecordDate())) {
            throw new IllegalArgumentException("Next visit date must be after the visit date");
        }
    }
}

package com.moh.moh_backend.service;

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

        // Auto-adjust growth status by WHO standard percentiles if off-range
        Integer ageMonths = babyRecord.getAgeMonths();
        if (ageMonths == null && baby.getDateOfBirth() != null && babyRecord.getRecordDate() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(baby.getDateOfBirth(), babyRecord.getRecordDate());
            ageMonths = (int) Math.max(0, days / 30.44);
            babyRecord.setAgeMonths(ageMonths);
        }
        if (babyRecord.getWeight() != null && ageMonths != null) {
            var whoStatus = evaluateWhoGrowthStatus(babyRecord.getWeight(), ageMonths);
            if (whoStatus != null && whoStatus != com.moh.moh_backend.model.GrowthStatus.NORMAL) {
                if (babyRecord.getGrowthStatus() == null || babyRecord.getGrowthStatus() == com.moh.moh_backend.model.GrowthStatus.NORMAL) {
                    babyRecord.setGrowthStatus(whoStatus);
                }
            }
        }

        // Auto-flag high risks into baby entity if severe condition detected
        boolean isHighRisk = false;
        String riskDetail = "";
        if (babyRecord.getGrowthStatus() != null && 
            (babyRecord.getGrowthStatus() == com.moh.moh_backend.model.GrowthStatus.UNDERWEIGHT || 
             babyRecord.getGrowthStatus() == com.moh.moh_backend.model.GrowthStatus.WASTED)) {
            isHighRisk = true;
            riskDetail = "Growth: " + babyRecord.getGrowthStatus().name();
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

        // Auto-adjust growth status by WHO standard percentiles if off-range
        Integer updAge = existing.getAgeMonths();
        if (updAge == null && existing.getBaby() != null && existing.getBaby().getDateOfBirth() != null && existing.getRecordDate() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(existing.getBaby().getDateOfBirth(), existing.getRecordDate());
            updAge = (int) Math.max(0, days / 30.44);
            existing.setAgeMonths(updAge);
        }
        if (existing.getWeight() != null && updAge != null) {
            var whoStatus = evaluateWhoGrowthStatus(existing.getWeight(), updAge);
            if (whoStatus != null && whoStatus != com.moh.moh_backend.model.GrowthStatus.NORMAL) {
                if (existing.getGrowthStatus() == null || existing.getGrowthStatus() == com.moh.moh_backend.model.GrowthStatus.NORMAL) {
                    existing.setGrowthStatus(whoStatus);
                }
            }
        }

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

    public static com.moh.moh_backend.model.GrowthStatus evaluateWhoGrowthStatus(Number weightNum, Integer ageMonths) {
        if (weightNum == null || ageMonths == null || weightNum.doubleValue() <= 0 || ageMonths < 0) {
            return null;
        }
        double weight = weightNum.doubleValue();
        int m = Math.max(0, Math.min(60, ageMonths));
        double p3, p97;
        if (m <= 0) { p3 = 2.5; p97 = 4.4; }
        else if (m <= 2) { p3 = 2.5 + (m / 2.0) * (4.3 - 2.5); p97 = 4.4 + (m / 2.0) * (7.1 - 4.4); }
        else if (m <= 6) { p3 = 4.3 + ((m - 2) / 4.0) * (6.4 - 4.3); p97 = 7.1 + ((m - 2) / 4.0) * (9.8 - 7.1); }
        else if (m <= 12) { p3 = 6.4 + ((m - 6) / 6.0) * (7.7 - 6.4); p97 = 9.8 + ((m - 6) / 6.0) * (12.0 - 9.8); }
        else if (m <= 24) { p3 = 7.7 + ((m - 12) / 12.0) * (9.7 - 7.7); p97 = 12.0 + ((m - 12) / 12.0) * (15.3 - 12.0); }
        else if (m <= 36) { p3 = 9.7 + ((m - 24) / 12.0) * (11.3 - 9.7); p97 = 15.3 + ((m - 24) / 12.0) * (18.3 - 15.3); }
        else { p3 = 11.3 + ((m - 36) / 24.0) * (14.1 - 11.3); p97 = 18.3 + ((m - 36) / 24.0) * (24.2 - 18.3); }

        if (weight < p3) {
            return (weight < p3 * 0.85) ? com.moh.moh_backend.model.GrowthStatus.WASTED : com.moh.moh_backend.model.GrowthStatus.UNDERWEIGHT;
        } else if (weight > p97) {
            return com.moh.moh_backend.model.GrowthStatus.OVERWEIGHT;
        }
        return com.moh.moh_backend.model.GrowthStatus.NORMAL;
    }
}

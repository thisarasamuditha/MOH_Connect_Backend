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
        }

        // Validate and set doctor (optional)
        if (doctorId != null) {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
            babyRecord.setDoctor(doctor);
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
        return babyRecordRepository.findByBaby_BabyId(babyId);
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

        return babyRecordRepository.save(existing);
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
}

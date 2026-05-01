package com.moh.moh_backend.service;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.model.BabyRecord;
import com.moh.moh_backend.model.Doctor;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.repository.BabyRecordRepository;
import com.moh.moh_backend.repository.BabyRepository;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BabyRecordService {

    private final BabyRecordRepository babyRecordRepository;
    private final BabyRepository babyRepository;
    private final MidwifeRepository midwifeRepository;
    private final DoctorRepository doctorRepository;

    public BabyRecordService(BabyRecordRepository babyRecordRepository,
                             BabyRepository babyRepository,
                             MidwifeRepository midwifeRepository,
                             DoctorRepository doctorRepository) {
        this.babyRecordRepository = babyRecordRepository;
        this.babyRepository = babyRepository;
        this.midwifeRepository = midwifeRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public BabyRecord createBabyRecord(BabyRecord babyRecord, Integer babyId, 
                                       Integer midwifeId, Integer doctorId) {
        // Validate and set baby (required)
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new RuntimeException("Baby not found with id: " + babyId));
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

    public BabyRecord getBabyRecordById(Integer id) {
        return babyRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Baby record not found with id: " + id));
    }

    public List<BabyRecord> getBabyRecordsByBabyId(Integer babyId) {
        return babyRecordRepository.findByBaby_BabyId(babyId);
    }

    @Transactional
    public BabyRecord updateBabyRecord(Integer id, BabyRecord updatedRecord, 
                                       Integer midwifeId, Integer doctorId) {
        BabyRecord existing = getBabyRecordById(id);

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
    public void deleteBabyRecord(Integer id) {
        if (!babyRecordRepository.existsById(id)) {
            throw new RuntimeException("Baby record not found with id: " + id);
        }
        babyRecordRepository.deleteById(id);
    }
}

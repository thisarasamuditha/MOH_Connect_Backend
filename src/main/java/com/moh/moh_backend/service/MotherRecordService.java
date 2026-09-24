package com.moh.moh_backend.service;

import com.moh.moh_backend.model.Doctor;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.model.MotherRecord;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRecordRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class MotherRecordService {

    private final MotherRecordRepository motherRecordRepository;
    private final PregnancyRepository pregnancyRepository;
    private final MidwifeRepository midwifeRepository;
    private final DoctorRepository doctorRepository;
    private final PregnancyService pregnancyService;

    public MotherRecordService(MotherRecordRepository motherRecordRepository,
                               PregnancyRepository pregnancyRepository,
                               MidwifeRepository midwifeRepository,
                               DoctorRepository doctorRepository,
                               PregnancyService pregnancyService) {
        this.motherRecordRepository = motherRecordRepository;
        this.pregnancyRepository = pregnancyRepository;
        this.midwifeRepository = midwifeRepository;
        this.doctorRepository = doctorRepository;
        this.pregnancyService = pregnancyService;
    }

    @Transactional
    public MotherRecord createMotherRecord(MotherRecord motherRecord, Integer pregnancyId, 
                                           Integer midwifeId, Integer doctorId,
                                           Integer userId, String role) {
        pregnancyService.assertCanAccessPregnancy(pregnancyId, userId, role);
        // Validate and set pregnancy (required)
        Pregnancy pregnancy = pregnancyRepository.findById(pregnancyId)
                .orElseThrow(() -> new RuntimeException("Pregnancy not found with id: " + pregnancyId));
        motherRecord.setPregnancy(pregnancy);
        if (motherRecord.getVisitType() == null || motherRecord.getVisitType().isBlank()) {
            motherRecord.setVisitType("ANC");
        }
        if ("DOCTOR".equalsIgnoreCase(role)) {
            motherRecord.setVerificationStatus("APPROVED");
        } else {
            motherRecord.setVerificationStatus("SUBMITTED");
        }

        // Validate and set midwife (optional)
        if (midwifeId != null) {
            Midwife midwife = midwifeRepository.findById(midwifeId)
                    .orElseThrow(() -> new RuntimeException("Midwife not found with id: " + midwifeId));
            motherRecord.setMidwife(midwife);
        }

        // Validate and set doctor (optional)
        if (doctorId != null) {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
            motherRecord.setDoctor(doctor);
        }

        return motherRecordRepository.save(motherRecord);
    }

    public MotherRecord getMotherRecordById(Integer id, Integer userId, String role) {
        MotherRecord record = motherRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mother record not found with id: " + id));
        pregnancyService.assertCanAccessPregnancy(record.getPregnancy().getPregnancyId(), userId, role);
        return record;
    }

    public List<MotherRecord> getMotherRecordsByPregnancyId(Integer pregnancyId, Integer userId, String role) {
        pregnancyService.assertCanAccessPregnancy(pregnancyId, userId, role);
        return motherRecordRepository.findByPregnancy_PregnancyId(pregnancyId);
    }

    @Transactional
    public MotherRecord updateMotherRecord(Integer id, MotherRecord updatedRecord, 
                                           Integer midwifeId, Integer doctorId,
                                           Integer userId, String role) {
        MotherRecord existing = getMotherRecordById(id, userId, role);

        // Update fields
        if (updatedRecord.getRecordDate() != null) {
            existing.setRecordDate(updatedRecord.getRecordDate());
        }
        if (updatedRecord.getGestationalAge() != null) {
            existing.setGestationalAge(updatedRecord.getGestationalAge());
        }
        if (updatedRecord.getWeight() != null) {
            existing.setWeight(updatedRecord.getWeight());
        }
        if (updatedRecord.getBmi() != null) {
            existing.setBmi(updatedRecord.getBmi());
        }
        if (updatedRecord.getBloodPressure() != null) {
            existing.setBloodPressure(updatedRecord.getBloodPressure());
        }
        if (updatedRecord.getShf() != null) {
            existing.setShf(updatedRecord.getShf());
        }
        if (updatedRecord.getFindings() != null) {
            existing.setFindings(updatedRecord.getFindings());
        }
        if (updatedRecord.getRecommendations() != null) {
            existing.setRecommendations(updatedRecord.getRecommendations());
        }
        if (updatedRecord.getComplications() != null) {
            existing.setComplications(updatedRecord.getComplications());
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

        return motherRecordRepository.save(existing);
    }

    @Transactional
    public MotherRecord reviewMotherRecord(Integer id, String status, String comment,
                                           Integer userId, String role) {
        if (!"DOCTOR".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only doctors can review clinical records");
        }
        MotherRecord existing = getMotherRecordById(id, userId, role);
        if (!List.of("APPROVED", "FLAGGED").contains(status)) {
            throw new IllegalArgumentException("Review status must be APPROVED or FLAGGED");
        }
        existing.setVerificationStatus(status);
        existing.setReviewComment(comment);
        existing.setReviewedAt(LocalDateTime.now());
        return motherRecordRepository.save(existing);
    }

    @Transactional
    public void deleteMotherRecord(Integer id, Integer userId, String role) {
        MotherRecord existing = getMotherRecordById(id, userId, role);
        motherRecordRepository.delete(existing);
    }
}

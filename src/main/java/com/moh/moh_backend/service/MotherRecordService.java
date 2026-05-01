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

@Service
public class MotherRecordService {

    private final MotherRecordRepository motherRecordRepository;
    private final PregnancyRepository pregnancyRepository;
    private final MidwifeRepository midwifeRepository;
    private final DoctorRepository doctorRepository;

    public MotherRecordService(MotherRecordRepository motherRecordRepository,
                               PregnancyRepository pregnancyRepository,
                               MidwifeRepository midwifeRepository,
                               DoctorRepository doctorRepository) {
        this.motherRecordRepository = motherRecordRepository;
        this.pregnancyRepository = pregnancyRepository;
        this.midwifeRepository = midwifeRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public MotherRecord createMotherRecord(MotherRecord motherRecord, Integer pregnancyId, 
                                           Integer midwifeId, Integer doctorId) {
        // Validate and set pregnancy (required)
        Pregnancy pregnancy = pregnancyRepository.findById(pregnancyId)
                .orElseThrow(() -> new RuntimeException("Pregnancy not found with id: " + pregnancyId));
        motherRecord.setPregnancy(pregnancy);

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

    public MotherRecord getMotherRecordById(Integer id) {
        return motherRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mother record not found with id: " + id));
    }

    public List<MotherRecord> getMotherRecordsByPregnancyId(Integer pregnancyId) {
        return motherRecordRepository.findByPregnancy_PregnancyId(pregnancyId);
    }

    @Transactional
    public MotherRecord updateMotherRecord(Integer id, MotherRecord updatedRecord, 
                                           Integer midwifeId, Integer doctorId) {
        MotherRecord existing = getMotherRecordById(id);

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
    public void deleteMotherRecord(Integer id) {
        if (!motherRecordRepository.existsById(id)) {
            throw new RuntimeException("Mother record not found with id: " + id);
        }
        motherRecordRepository.deleteById(id);
    }
}

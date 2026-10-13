package com.moh.moh_backend.service;

import com.moh.moh_backend.Enum.PregnancyStatus;

import com.moh.moh_backend.dto.MotherRecordReviewItem;
import com.moh.moh_backend.model.Doctor;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.model.MotherRecord;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRecordRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import com.moh.moh_backend.util.ClinicalValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class MotherRecordService {

    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_FLAGGED = "FLAGGED";

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
                .orElseThrow(() -> new IllegalArgumentException("Pregnancy not found with id: " + pregnancyId));
        motherRecord.setPregnancy(pregnancy);
        if (motherRecord.getVisitType() == null || motherRecord.getVisitType().isBlank()) {
            motherRecord.setVisitType("ANC");
        }
        if ("ANC".equalsIgnoreCase(motherRecord.getVisitType())
                && pregnancy.getPregnancyStatus() != PregnancyStatus.ACTIVE) {
            throw new IllegalStateException("Antenatal visits can only be added to an active pregnancy");
        }
        validateRecordValues(motherRecord, pregnancy);

        if ("DOCTOR".equalsIgnoreCase(role)) {
            motherRecord.setVerificationStatus(STATUS_APPROVED);
            motherRecord.setReviewedAt(LocalDateTime.now());
        } else {
            motherRecord.setVerificationStatus(STATUS_SUBMITTED);
        }

        // Midwife: explicit id, otherwise the logged-in midwife
        if (midwifeId != null) {
            Midwife midwife = midwifeRepository.findById(midwifeId)
                    .orElseThrow(() -> new IllegalArgumentException("Midwife not found with id: " + midwifeId));
            motherRecord.setMidwife(midwife);
        } else if ("MIDWIFE".equalsIgnoreCase(role)) {
            midwifeRepository.findByUser_UserId(userId).ifPresent(motherRecord::setMidwife);
        }

        // Doctor: explicit id, otherwise the logged-in doctor
        if (doctorId != null) {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found with id: " + doctorId));
            motherRecord.setDoctor(doctor);
        } else if ("DOCTOR".equalsIgnoreCase(role)) {
            doctorRepository.findByUser_UserId(userId).ifPresent(motherRecord::setDoctor);
        }

        return motherRecordRepository.save(motherRecord);
    }

    public MotherRecord getMotherRecordById(Integer id, Integer userId, String role) {
        MotherRecord record = motherRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mother record not found with id: " + id));
        pregnancyService.assertCanAccessPregnancy(record.getPregnancy().getPregnancyId(), userId, role);
        return record;
    }

    public List<MotherRecord> getMotherRecordsByPregnancyId(Integer pregnancyId, Integer userId, String role) {
        pregnancyService.assertCanAccessPregnancy(pregnancyId, userId, role);
        return motherRecordRepository.findByPregnancy_PregnancyIdOrderByRecordDateAscRecordIdAsc(pregnancyId);
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
        if (updatedRecord.getHemoglobin() != null) {
            existing.setHemoglobin(updatedRecord.getHemoglobin());
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
        validateRecordValues(existing, existing.getPregnancy());

        // Update midwife if provided
        if (midwifeId != null) {
            Midwife midwife = midwifeRepository.findById(midwifeId)
                    .orElseThrow(() -> new IllegalArgumentException("Midwife not found with id: " + midwifeId));
            existing.setMidwife(midwife);
        }

        // Update doctor if provided
        if (doctorId != null) {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found with id: " + doctorId));
            existing.setDoctor(doctor);
        }

        // A midwife correcting a record sends it back to the doctor for review.
        // The previous review comment is kept so the doctor can see what was asked.
        if ("MIDWIFE".equalsIgnoreCase(role)) {
            existing.setVerificationStatus(STATUS_SUBMITTED);
        }

        return motherRecordRepository.save(existing);
    }

    @Transactional
    public MotherRecord reviewMotherRecord(Integer id, String status, String comment,
                                           Integer userId, String role) {
        if (!"DOCTOR".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only doctors can review clinical records");
        }
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!List.of(STATUS_APPROVED, STATUS_FLAGGED).contains(normalized)) {
            throw new IllegalArgumentException("Review status must be APPROVED or FLAGGED");
        }
        String trimmedComment = comment == null ? null : comment.trim();
        if (STATUS_FLAGGED.equals(normalized) && (trimmedComment == null || trimmedComment.isEmpty())) {
            throw new IllegalArgumentException("A comment is required when flagging a record, so the midwife knows what to correct");
        }

        MotherRecord existing = getMotherRecordById(id, userId, role);
        Doctor doctor = doctorRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalStateException("Doctor profile not found for this account"));
        assertDoctorCoversArea(doctor, existing.getPregnancy());

        existing.setVerificationStatus(normalized);
        existing.setReviewComment(trimmedComment == null || trimmedComment.isEmpty() ? null : trimmedComment);
        existing.setReviewedAt(LocalDateTime.now());
        existing.setDoctor(doctor);
        return motherRecordRepository.save(existing);
    }

    /**
     * Records for the caller's PHM area. Doctors use it as the review queue (default SUBMITTED);
     * midwives use it to see what the doctor flagged (default FLAGGED). Pass status=ALL for everything.
     */
    @Transactional(readOnly = true)
    public List<MotherRecordReviewItem> getReviewQueue(String status, Integer userId, String role) {
        Integer areaId;
        String defaultStatus;
        if ("DOCTOR".equalsIgnoreCase(role)) {
            Doctor doctor = doctorRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Doctor profile not found for this account"));
            if (doctor.getPhmArea() == null) {
                throw new IllegalStateException("Doctor has no PHM area assigned");
            }
            areaId = doctor.getPhmArea().getPhmAreaId();
            defaultStatus = STATUS_SUBMITTED;
        } else if ("MIDWIFE".equalsIgnoreCase(role)) {
            Midwife midwife = midwifeRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Midwife profile not found for this account"));
            if (midwife.getPhmArea() == null) {
                throw new IllegalStateException("Midwife has no PHM area assigned");
            }
            areaId = midwife.getPhmArea().getPhmAreaId();
            defaultStatus = STATUS_FLAGGED;
        } else {
            throw new IllegalStateException("Only doctors and midwives have a review queue");
        }

        String filter;
        if (status == null || status.isBlank()) {
            filter = defaultStatus;
        } else if ("ALL".equalsIgnoreCase(status.trim())) {
            filter = null;
        } else {
            filter = status.trim().toUpperCase();
            if (!List.of(STATUS_SUBMITTED, STATUS_APPROVED, STATUS_FLAGGED).contains(filter)) {
                throw new IllegalArgumentException("Status must be SUBMITTED, APPROVED, FLAGGED or ALL");
            }
        }

        return motherRecordRepository.findForReview(areaId, filter).stream()
                .map(MotherRecordReviewItem::from)
                .toList();
    }

    @Transactional
    public void deleteMotherRecord(Integer id, Integer userId, String role) {
        MotherRecord existing = getMotherRecordById(id, userId, role);
        motherRecordRepository.delete(existing);
    }

    private void assertDoctorCoversArea(Doctor doctor, Pregnancy pregnancy) {
        if (doctor.getPhmArea() == null || pregnancy == null || pregnancy.getMother() == null
                || pregnancy.getMother().getPhmArea() == null) {
            return;
        }
        if (!doctor.getPhmArea().getPhmAreaId().equals(pregnancy.getMother().getPhmArea().getPhmAreaId())) {
            throw new IllegalStateException("Doctors can only review records from their own PHM area");
        }
    }

    private void validateRecordValues(MotherRecord r, Pregnancy pregnancy) {
        if (r.getRecordDate() == null) {
            throw new IllegalArgumentException("Visit date is required");
        }
        ClinicalValidation.notInFuture(r.getRecordDate(), "Visit date");
        if (pregnancy != null) {
            ClinicalValidation.notBefore(r.getRecordDate(), pregnancy.getLmpDate(), "Visit date", "the LMP date");
        }
        ClinicalValidation.range(r.getGestationalAge(), 0, 45, "Gestational age (weeks)");
        ClinicalValidation.range(r.getWeight(), 25, 200, "Weight (kg)");
        ClinicalValidation.range(r.getBmi(), 10, 70, "BMI");
        ClinicalValidation.range(r.getShf(), 5, 50, "SFH (cm)");
        ClinicalValidation.range(r.getHemoglobin(), 3, 20, "Haemoglobin (g/dL)");
        ClinicalValidation.bloodPressure(r.getBloodPressure());
        if (r.getNextVisitDate() != null && !r.getNextVisitDate().isAfter(r.getRecordDate())) {
            throw new IllegalArgumentException("Next visit date must be after the visit date");
        }
    }
}

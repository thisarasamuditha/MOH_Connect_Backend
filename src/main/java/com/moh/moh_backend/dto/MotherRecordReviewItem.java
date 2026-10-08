package com.moh.moh_backend.dto;

import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.MotherRecord;
import com.moh.moh_backend.model.Pregnancy;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Flattened ANC record with mother/pregnancy context, used by the doctor review queue and the midwife "flagged" list. */
public class MotherRecordReviewItem {
    public Integer recordId;
    public Integer pregnancyId;
    public String pregnancyNumber;
    public String pregnancyStatus;
    public String riskLevel;
    public Integer motherId;
    public String motherName;
    public String motherNic;
    public String motherContact;

    public LocalDate recordDate;
    public String visitType;
    public Integer gestationalAge;
    public Float weight;
    public Float bmi;
    public String bloodPressure;
    public Float shf;
    public Float hemoglobin;
    public String findings;
    public String recommendations;
    public String complications;
    public String notes;
    public LocalDate nextVisitDate;

    public String verificationStatus;
    public String reviewComment;
    public LocalDateTime reviewedAt;
    public String reviewedBy;
    public String recordedBy;

    public static MotherRecordReviewItem from(MotherRecord r) {
        MotherRecordReviewItem dto = new MotherRecordReviewItem();
        dto.recordId = r.getRecordId();
        Pregnancy p = r.getPregnancy();
        if (p != null) {
            dto.pregnancyId = p.getPregnancyId();
            dto.pregnancyNumber = p.getPregnancyNumber();
            dto.pregnancyStatus = p.getPregnancyStatus() != null ? p.getPregnancyStatus().name() : null;
            dto.riskLevel = p.getRiskLevel() != null ? p.getRiskLevel().name() : null;
            Mother m = p.getMother();
            if (m != null) {
                dto.motherId = m.getMotherId();
                dto.motherName = m.getName();
                dto.motherNic = m.getNic();
                dto.motherContact = m.getContactNumber();
            }
        }
        dto.recordDate = r.getRecordDate();
        dto.visitType = r.getVisitType();
        dto.gestationalAge = r.getGestationalAge();
        dto.weight = r.getWeight();
        dto.bmi = r.getBmi();
        dto.bloodPressure = r.getBloodPressure();
        dto.shf = r.getShf();
        dto.hemoglobin = r.getHemoglobin();
        dto.findings = r.getFindings();
        dto.recommendations = r.getRecommendations();
        dto.complications = r.getComplications();
        dto.notes = r.getNotes();
        dto.nextVisitDate = r.getNextVisitDate();
        dto.verificationStatus = r.getVerificationStatus();
        dto.reviewComment = r.getReviewComment();
        dto.reviewedAt = r.getReviewedAt();
        dto.reviewedBy = r.getDoctor() != null ? r.getDoctor().getName() : null;
        dto.recordedBy = r.getMidwife() != null ? r.getMidwife().getName() : null;
        return dto;
    }
}

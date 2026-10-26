package com.moh.moh_backend.dto;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.model.Doctor;
import com.moh.moh_backend.model.DoctorAppointment;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DoctorAppointmentDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String patientType; // "MOTHER" or "BABY"
        private Integer motherId;
        private Integer pregnancyId;
        private Integer babyId;
        private Integer doctorId;
        private String reason; // Mandatory
        private String priority; // "ROUTINE", "URGENT", "HIGH_RISK"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleRequest {
        private LocalDate appointmentDate;
        private String appointmentTime; // e.g. "10:00 AM" or "14:30"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompleteConsultationRequest {
        private String clinicalNotes;
        private String diagnosis;
        private String prescriptions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DoctorInfo {
        private Integer doctorId;
        private String name;
        private String specialization;
        private Integer phmAreaId;
        private String phmAreaName;
        private String contactNumber;
        private String email;
        private String licenseNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PatientInfo {
        // Common
        private String type; // MOTHER or BABY
        private String name;
        private String contactNumber;
        private String address;

        // Mother / Pregnancy specific
        private Integer motherId;
        private String nic;
        private Integer pregnancyId;
        private String pregnancyNumber;
        private LocalDate lmpDate;
        private LocalDate eddDate;
        private Integer currentWeek;
        private String riskLevel;
        private String riskFactors;
        private String bloodGroup;

        // Baby specific
        private Integer babyId;
        private String babyName;
        private String gender;
        private LocalDate dateOfBirth;
        private Double birthWeight;
        private String motherName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Integer appointmentId;
        private String patientType;
        private String status;
        private String priority;
        private String reason;
        private LocalDate appointmentDate;
        private String appointmentTime;
        private LocalDateTime scheduledAt;
        private String clinicalNotes;
        private String diagnosis;
        private String prescriptions;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private DoctorInfo doctor;
        private PatientInfo patient;
        private String referredByName;

        public static Response from(DoctorAppointment a) {
            if (a == null) return null;

            DoctorInfo docInfo = null;
            if (a.getDoctor() != null) {
                Doctor d = a.getDoctor();
                docInfo = DoctorInfo.builder()
                        .doctorId(d.getDoctorId())
                        .name(d.getName())
                        .specialization(d.getSpecialization())
                        .phmAreaId(d.getPhmArea() != null ? d.getPhmArea().getPhmAreaId() : null)
                        .phmAreaName(d.getPhmArea() != null ? d.getPhmArea().getAreaName() : null)
                        .contactNumber(d.getContactNumber())
                        .email(d.getEmail())
                        .licenseNumber(d.getLicenseNumber())
                        .build();
            }

            PatientInfo patInfo = null;
            if ("MOTHER".equalsIgnoreCase(a.getPatientType().name())) {
                Mother m = a.getMother();
                Pregnancy p = a.getPregnancy();
                patInfo = PatientInfo.builder()
                        .type("MOTHER")
                        .motherId(m != null ? m.getMotherId() : null)
                        .name(m != null ? m.getName() : null)
                        .nic(m != null ? m.getNic() : null)
                        .contactNumber(m != null ? m.getContactNumber() : null)
                        .address(m != null ? m.getAddress() : null)
                        .bloodGroup(m != null ? m.getBloodGroup() : null)
                        .pregnancyId(p != null ? p.getPregnancyId() : null)
                        .pregnancyNumber(p != null ? p.getPregnancyNumber() : null)
                        .lmpDate(p != null ? p.getLmpDate() : null)
                        .eddDate(p != null ? p.getEddDate() : null)
                        .riskLevel(p != null && p.getRiskLevel() != null ? p.getRiskLevel().name() : null)
                        .riskFactors(p != null ? p.getRiskFactors() : null)
                        .build();

                if (p != null && p.getLmpDate() != null) {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(p.getLmpDate(), LocalDate.now());
                    patInfo.setCurrentWeek((int) Math.max(0, days / 7));
                }
            } else if ("BABY".equalsIgnoreCase(a.getPatientType().name())) {
                Baby b = a.getBaby();
                Mother m = a.getMother();
                patInfo = PatientInfo.builder()
                        .type("BABY")
                        .babyId(b != null ? b.getBabyId() : null)
                        .name(b != null ? (b.getName() != null ? b.getName() : "Baby") : null)
                        .gender(b != null && b.getGender() != null ? b.getGender().name() : null)
                        .dateOfBirth(b != null ? b.getDateOfBirth() : null)
                        .birthWeight(b != null && b.getBirthWeight() != null ? b.getBirthWeight().doubleValue() : null)
                        .motherId(m != null ? m.getMotherId() : null)
                        .motherName(m != null ? m.getName() : null)
                        .contactNumber(m != null ? m.getContactNumber() : null)
                        .address(m != null ? m.getAddress() : null)
                        .build();
            }

            String refName = null;
            if (a.getReferredBy() != null) {
                refName = a.getReferredBy().getUsername();
            }

            return Response.builder()
                    .appointmentId(a.getAppointmentId())
                    .patientType(a.getPatientType() != null ? a.getPatientType().name() : null)
                    .status(a.getStatus() != null ? a.getStatus().name() : null)
                    .priority(a.getPriority() != null ? a.getPriority().name() : null)
                    .reason(a.getReason())
                    .appointmentDate(a.getAppointmentDate())
                    .appointmentTime(a.getAppointmentTime())
                    .scheduledAt(a.getScheduledAt())
                    .clinicalNotes(a.getClinicalNotes())
                    .diagnosis(a.getDiagnosis())
                    .prescriptions(a.getPrescriptions())
                    .completedAt(a.getCompletedAt())
                    .createdAt(a.getCreatedAt())
                    .updatedAt(a.getUpdatedAt())
                    .doctor(docInfo)
                    .patient(patInfo)
                    .referredByName(refName)
                    .build();
        }
    }
}

package com.moh.moh_backend.Enum;

public enum DoctorAppointmentStatus {
    DRAFT,           // Initial state when referred/assigned by midwife, awaiting doctor scheduling
    PENDING_REVIEW,  // Doctor scheduled appointment date & time, awaiting consultation
    COMPLETED,       // Doctor conducted consultation, filled clinical notes, diagnosis & prescriptions
    CANCELLED
}

package com.moh.moh_backend.service;

import com.moh.moh_backend.Enum.AppointmentPriority;
import com.moh.moh_backend.Enum.DoctorAppointmentStatus;
import com.moh.moh_backend.Enum.PatientType;
import com.moh.moh_backend.dto.DoctorAppointmentDtos;
import com.moh.moh_backend.model.*;
import com.moh.moh_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorAppointmentService {

    private final DoctorAppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final MotherRepository motherRepository;
    private final PregnancyRepository pregnancyRepository;
    private final BabyRepository babyRepository;
    private final UserRepository userRepository;

    @Transactional
    public DoctorAppointmentDtos.Response createAppointment(DoctorAppointmentDtos.CreateRequest req, Integer userId, String role) {
        if (req == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (req.getDoctorId() == null) {
            throw new IllegalArgumentException("Doctor ID is required to assign");
        }
        if (req.getReason() == null || req.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Reason (හේතුව) is required when assigning a patient to a doctor");
        }

        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with id: " + req.getDoctorId()));

        User referredBy = userId != null ? userRepository.findById(userId).orElse(null) : null;

        String typeStr = req.getPatientType() != null ? req.getPatientType().trim().toUpperCase() : "MOTHER";
        PatientType patientType;
        try {
            patientType = PatientType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            patientType = PatientType.MOTHER;
        }

        Mother mother = null;
        Pregnancy pregnancy = null;
        Baby baby = null;

        if (patientType == PatientType.MOTHER) {
            if (req.getPregnancyId() != null) {
                pregnancy = pregnancyRepository.findById(req.getPregnancyId())
                        .orElseThrow(() -> new IllegalArgumentException("Pregnancy not found with id: " + req.getPregnancyId()));
                mother = pregnancy.getMother();
            } else if (req.getMotherId() != null) {
                mother = motherRepository.findById(req.getMotherId())
                        .orElseThrow(() -> new IllegalArgumentException("Mother not found with id: " + req.getMotherId()));
            } else {
                throw new IllegalArgumentException("Either pregnancyId or motherId is required for MOTHER referral");
            }
        } else if (patientType == PatientType.BABY) {
            if (req.getBabyId() == null) {
                throw new IllegalArgumentException("Baby ID is required for BABY referral");
            }
            baby = babyRepository.findById(req.getBabyId())
                    .orElseThrow(() -> new IllegalArgumentException("Baby not found with id: " + req.getBabyId()));
            if (baby.getMotherId() != null) {
                mother = motherRepository.findById(baby.getMotherId()).orElse(null);
            }
            if (baby.getPregnancyId() != null) {
                pregnancy = pregnancyRepository.findById(baby.getPregnancyId()).orElse(null);
            }
        }

        AppointmentPriority priority = AppointmentPriority.HIGH_RISK;
        if (req.getPriority() != null && !req.getPriority().isBlank()) {
            try {
                priority = AppointmentPriority.valueOf(req.getPriority().trim().toUpperCase());
            } catch (Exception ignored) {
            }
        }

        DoctorAppointment appointment = DoctorAppointment.builder()
                .patientType(patientType)
                .mother(mother)
                .pregnancy(pregnancy)
                .baby(baby)
                .doctor(doctor)
                .referredBy(referredBy)
                .reason(req.getReason().trim())
                .priority(priority)
                .status(DoctorAppointmentStatus.DRAFT) // Initial status is DRAFT
                .build();

        DoctorAppointment saved = appointmentRepository.save(appointment);
        log.info("Doctor appointment created with id: {} for doctor: {} by user: {}", saved.getAppointmentId(), doctor.getDoctorId(), userId);
        return DoctorAppointmentDtos.Response.from(saved);
    }

    public List<DoctorAppointmentDtos.Response> getAppointments(String statusFilter, Integer doctorId, Integer userId, String role) {
        List<DoctorAppointment> list;

        if (doctorId != null) {
            if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
                try {
                    DoctorAppointmentStatus s = DoctorAppointmentStatus.valueOf(statusFilter.trim().toUpperCase());
                    list = appointmentRepository.findByDoctor_DoctorIdAndStatusOrderByCreatedAtDesc(doctorId, s);
                } catch (IllegalArgumentException e) {
                    list = appointmentRepository.findByDoctor_DoctorIdOrderByCreatedAtDesc(doctorId);
                }
            } else {
                list = appointmentRepository.findByDoctor_DoctorIdOrderByCreatedAtDesc(doctorId);
            }
        } else if ("DOCTOR".equalsIgnoreCase(role)) {
            if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
                try {
                    DoctorAppointmentStatus s = DoctorAppointmentStatus.valueOf(statusFilter.trim().toUpperCase());
                    list = appointmentRepository.findByDoctor_User_UserIdAndStatusOrderByCreatedAtDesc(userId, s);
                } catch (IllegalArgumentException e) {
                    list = appointmentRepository.findByDoctor_User_UserIdOrderByCreatedAtDesc(userId);
                }
            } else {
                list = appointmentRepository.findByDoctor_User_UserIdOrderByCreatedAtDesc(userId);
            }
        } else {
            // ADMIN or MIDWIFE viewing appointments
            list = appointmentRepository.findAll();
            if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
                try {
                    DoctorAppointmentStatus s = DoctorAppointmentStatus.valueOf(statusFilter.trim().toUpperCase());
                    list = list.stream().filter(a -> a.getStatus() == s).collect(Collectors.toList());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return list.stream().map(DoctorAppointmentDtos.Response::from).collect(Collectors.toList());
    }

    public List<DoctorAppointmentDtos.Response> getAppointments(String statusFilter, Integer userId, String role) {
        return getAppointments(statusFilter, null, userId, role);
    }

    /**
     * User requirement: Consultations view only shows PENDING_REVIEW and COMPLETED appointments!
     */
    public List<DoctorAppointmentDtos.Response> getConsultations(Integer doctorId, Integer userId, String role) {
        List<DoctorAppointmentStatus> consultationStatuses = Arrays.asList(
                DoctorAppointmentStatus.PENDING_REVIEW,
                DoctorAppointmentStatus.COMPLETED
        );

        List<DoctorAppointment> list;
        if (doctorId != null) {
            list = appointmentRepository.findByDoctor_DoctorIdAndStatusInOrderByAppointmentDateAscAppointmentTimeAscCreatedAtDesc(
                    doctorId, consultationStatuses);
        } else if ("DOCTOR".equalsIgnoreCase(role)) {
            list = appointmentRepository.findByDoctor_User_UserIdAndStatusInOrderByAppointmentDateAscAppointmentTimeAscCreatedAtDesc(
                    userId, consultationStatuses);
        } else {
            list = appointmentRepository.findByStatusInOrderByAppointmentDateAscAppointmentTimeAscCreatedAtDesc(consultationStatuses);
        }

        return list.stream().map(DoctorAppointmentDtos.Response::from).collect(Collectors.toList());
    }

    public List<DoctorAppointmentDtos.Response> getConsultations(Integer userId, String role) {
        return getConsultations(null, userId, role);
    }

    @Transactional
    public DoctorAppointmentDtos.Response scheduleAppointment(Integer appointmentId, DoctorAppointmentDtos.ScheduleRequest req, Integer userId, String role) {
        DoctorAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + appointmentId));

        assertDoctorCanManage(appointment, userId, role);

        if (req == null || req.getAppointmentDate() == null) {
            throw new IllegalArgumentException("Appointment date is required");
        }
        if (req.getAppointmentTime() == null || req.getAppointmentTime().isBlank()) {
            throw new IllegalArgumentException("Appointment time is required");
        }

        appointment.setAppointmentDate(req.getAppointmentDate());
        appointment.setAppointmentTime(req.getAppointmentTime().trim());
        appointment.setScheduledAt(LocalDateTime.now());
        appointment.setStatus(DoctorAppointmentStatus.PENDING_REVIEW);

        DoctorAppointment saved = appointmentRepository.save(appointment);
        log.info("Appointment {} scheduled for date: {} time: {} by doctor user {}", appointmentId, req.getAppointmentDate(), req.getAppointmentTime(), userId);
        return DoctorAppointmentDtos.Response.from(saved);
    }

    @Transactional
    public DoctorAppointmentDtos.Response completeConsultation(Integer appointmentId, DoctorAppointmentDtos.CompleteConsultationRequest req, Integer userId, String role) {
        DoctorAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + appointmentId));

        assertDoctorCanManage(appointment, userId, role);

        if (req == null) {
            throw new IllegalArgumentException("Consultation notes request cannot be empty");
        }
        if (req.getClinicalNotes() == null || req.getClinicalNotes().isBlank()) {
            throw new IllegalArgumentException("Clinical notes are required to complete the consultation");
        }

        appointment.setClinicalNotes(req.getClinicalNotes().trim());
        appointment.setDiagnosis(req.getDiagnosis() != null ? req.getDiagnosis().trim() : null);
        appointment.setPrescriptions(req.getPrescriptions() != null ? req.getPrescriptions().trim() : null);
        appointment.setStatus(DoctorAppointmentStatus.COMPLETED);
        appointment.setCompletedAt(LocalDateTime.now());

        DoctorAppointment saved = appointmentRepository.save(appointment);
        log.info("Appointment {} completed with clinical notes by doctor user {}", appointmentId, userId);
        return DoctorAppointmentDtos.Response.from(saved);
    }

    public DoctorAppointmentDtos.Response getAppointmentById(Integer appointmentId, Integer userId, String role) {
        DoctorAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + appointmentId));
        return DoctorAppointmentDtos.Response.from(appointment);
    }

    public List<DoctorAppointmentDtos.DoctorInfo> getAllDoctors() {
        return doctorRepository.findAll().stream().map(d -> DoctorAppointmentDtos.DoctorInfo.builder()
                .doctorId(d.getDoctorId())
                .name(d.getName())
                .specialization(d.getSpecialization())
                .phmAreaId(d.getPhmArea() != null ? d.getPhmArea().getPhmAreaId() : null)
                .phmAreaName(d.getPhmArea() != null ? d.getPhmArea().getAreaName() : null)
                .contactNumber(d.getContactNumber())
                .email(d.getEmail())
                .licenseNumber(d.getLicenseNumber())
                .build()
        ).collect(Collectors.toList());
    }

    private void assertDoctorCanManage(DoctorAppointment appointment, Integer userId, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) return;
        if (!"DOCTOR".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only doctors can schedule or complete consultations");
        }
        if (appointment.getDoctor() == null || appointment.getDoctor().getUser() == null ||
                !userId.equals(appointment.getDoctor().getUser().getUserId())) {
            throw new IllegalStateException("You can only manage appointments assigned to your account");
        }
    }
}

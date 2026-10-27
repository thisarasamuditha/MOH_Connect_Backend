package com.moh.moh_backend.repository;

import com.moh.moh_backend.Enum.DoctorAppointmentStatus;
import com.moh.moh_backend.model.DoctorAppointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DoctorAppointmentRepository extends JpaRepository<DoctorAppointment, Integer> {

    List<DoctorAppointment> findByDoctor_DoctorIdOrderByCreatedAtDesc(Integer doctorId);

    List<DoctorAppointment> findByDoctor_DoctorIdAndStatusOrderByCreatedAtDesc(Integer doctorId, DoctorAppointmentStatus status);

    List<DoctorAppointment> findByDoctor_DoctorIdAndStatusInOrderByAppointmentDateAscAppointmentTimeAscCreatedAtDesc(
            Integer doctorId, Collection<DoctorAppointmentStatus> statuses);

    List<DoctorAppointment> findByDoctor_User_UserIdOrderByCreatedAtDesc(Integer userId);

    List<DoctorAppointment> findByDoctor_User_UserIdAndStatusOrderByCreatedAtDesc(Integer userId, DoctorAppointmentStatus status);

    List<DoctorAppointment> findByDoctor_User_UserIdAndStatusInOrderByAppointmentDateAscAppointmentTimeAscCreatedAtDesc(
            Integer userId, Collection<DoctorAppointmentStatus> statuses);

    List<DoctorAppointment> findByStatusInOrderByAppointmentDateAscAppointmentTimeAscCreatedAtDesc(
            Collection<DoctorAppointmentStatus> statuses);

    List<DoctorAppointment> findByMother_MotherIdOrderByCreatedAtDesc(Integer motherId);

    List<DoctorAppointment> findByBaby_BabyIdOrderByCreatedAtDesc(Integer babyId);
}

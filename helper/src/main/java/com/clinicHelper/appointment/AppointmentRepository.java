package com.clinicHelper.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AppointmentRepository  extends JpaRepository<Appointment, Long> {
    @Query("SELECT a FROM Appointment a WHERE a.patient.user.id = :patientId")
    List<Appointment> findByPatientId(@Param("patientId") Integer patientId);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.user.id = :doctorId")
    List<Appointment> findByDoctorId(@Param("doctorId") Integer doctorId);
    
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.user.id = :doctorId AND a.appointmentTime = :appointmentTime")
    boolean existsByDoctorIdAndAppointmentTime(@Param("doctorId") Integer doctorId, @Param("appointmentTime") LocalDateTime appointmentTime);
}

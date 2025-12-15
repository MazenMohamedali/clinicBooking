package com.clinicHelper.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AppointmentRepository  extends JpaRepository<Appointment, Long> {
        @Query(value = "SELECT * FROM appointment WHERE patient_id = :patientId", nativeQuery = true)
    List<Appointment> findByPatientId(@Param("patientId") Integer patientId);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.user.id = :doctorId")
    List<Appointment> findByDoctorId(@Param("doctorId") Integer doctorId);

    @Query("SELECT a FROM Appointment a WHERE a.clinic.clinicId = :clinicId")
    List<Appointment> findByClinicId(@Param("clinicId") Integer clinicId);
    
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.user.id = :doctorId AND a.appointmentTime = :appointmentTime")
    boolean existsByDoctorIdAndAppointmentTime(@Param("doctorId") Integer doctorId, @Param("appointmentTime") LocalDateTime appointmentTime);

    @Query(value = "SELECT * FROM appointment ORDER BY created_at DESC LIMIT 10", nativeQuery = true)
    List<Appointment> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countAppointmentsByStatus();

    @Query("SELECT a FROM Appointment a WHERE a.appointmentTime BETWEEN :startDate AND :endDate")
    List<Appointment> findByAppointmentTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}

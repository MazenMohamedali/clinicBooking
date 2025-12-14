package com.clinicHelper.doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.clinicHelper.user.User;

@Repository
public interface DoctorClinicRepository extends JpaRepository<DoctorClinic, DoctorClinicId> {
    
    @Query("SELECT CASE WHEN COUNT(dc) > 0 THEN true ELSE false END " +
           "FROM DoctorClinic dc WHERE dc.doctor = :doctor AND dc.clinic = :clinic")
    boolean existsByDoctorAndClinic(@Param("doctor") User doctor, @Param("clinic") Clinic clinic);
}
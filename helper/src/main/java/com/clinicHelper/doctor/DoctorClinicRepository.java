package com.clinicHelper.doctor;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.clinicHelper.user.User;

@Repository
public interface DoctorClinicRepository extends JpaRepository<DoctorClinic, DoctorClinicId> {
    boolean existsByDoctorAndClinic(User doctor, Clinic clinic);
    Optional<DoctorClinic> findFirstByDoctor(User doctor);
}
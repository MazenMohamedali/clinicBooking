package com.clinicHelper.Receptionist;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinicHelper.doctor.Clinic;
import com.clinicHelper.user.User;

@Repository
public interface ReceptionistClinicRepository extends JpaRepository<ReceptionistClinic, ReceptionistClinicId> {
    
    @Query("SELECT rc FROM ReceptionistClinic rc WHERE rc.assignedByDoctor.id = :doctorId")
    List<ReceptionistClinic> findByAssignedByDoctor(@Param("doctorId") Integer doctorId);
    
    @Query("SELECT rc FROM ReceptionistClinic rc WHERE rc.receptionist.id = :receptionistId")
    List<ReceptionistClinic> findByReceptionistId(@Param("receptionistId") Integer receptionistId);
    
    boolean existsByReceptionistAndClinic(User receptionist, Clinic clinic);
}
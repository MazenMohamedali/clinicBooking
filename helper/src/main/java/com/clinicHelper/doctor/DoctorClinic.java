package com.clinicHelper.doctor;

import com.clinicHelper.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "doctor_clinic")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorClinic {
    
    @EmbeddedId
    private DoctorClinicId id;
    
    @ManyToOne
    @MapsId("doctorId")
    @JoinColumn(name = "doctor_id")
    private User doctor;
    
    @ManyToOne
    @MapsId("clinicId")
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;
}
package com.clinicHelper.Receptionist;

import com.clinicHelper.doctor.Clinic;
import com.clinicHelper.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "receptionist_clinic")
@Data
@NoArgsConstructor  
@AllArgsConstructor
@Builder
public class ReceptionistClinic {
    
    @EmbeddedId
    private ReceptionistClinicId id;
    
    @ManyToOne
    @MapsId("receptionistId")
    @JoinColumn(name = "receptionist_id")
    private ReceptionistProfile receptionist;
    
    @ManyToOne
    @MapsId("clinicId")
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;
    
    @ManyToOne
    @JoinColumn(name = "assigned_by_doctor")
    private User assignedByDoctor;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
}
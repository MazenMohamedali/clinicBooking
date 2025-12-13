package com.clinicHelper.doctor;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "doctor_clinic")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DoctorClinic {
    
    @Id
    @Column(name = "doctor_id")
    private Integer doctorId;

    @Id
    @Column(name = "clinic_id")
    private Integer clinicId;
}

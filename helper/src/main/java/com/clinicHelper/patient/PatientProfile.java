package com.clinicHelper.patient;

import java.time.LocalDate;

import com.clinicHelper.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patient_profile")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatientProfile {
    @Id
    private Integer userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    private String insuranceNumber;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(name="dob")
    private LocalDate birthDate;
}

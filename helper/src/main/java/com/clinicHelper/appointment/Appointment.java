package com.clinicHelper.appointment;

import java.time.LocalDateTime;

import com.clinicHelper.doctor.DoctorProfile;
import com.clinicHelper.patient.PatientProfile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    @Column(nullable = false)
    private LocalDateTime appointmentTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    public Appointment() {}

    public Appointment(Long id, PatientProfile patient, DoctorProfile doctor, LocalDateTime appointmentTime, AppointmentStatus status) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    public Long getId() { return id; }
    public PatientProfile getPatient() { return patient; }
    public DoctorProfile getDoctor() { return doctor; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public AppointmentStatus getStatus() { return status; }

    public void setId(Long id) { this.id = id; }
    public void setPatient(PatientProfile patient) { this.patient = patient; }
    public void setDoctor(DoctorProfile doctor) { this.doctor = doctor; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public static AppointmentBuilder builder() {
        return new AppointmentBuilder();
    }
}

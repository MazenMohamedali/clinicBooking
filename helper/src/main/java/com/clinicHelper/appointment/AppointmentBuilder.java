package com.clinicHelper.appointment;

import java.time.LocalDateTime;

import com.clinicHelper.doctor.DoctorProfile;
import com.clinicHelper.patient.PatientProfile;

public class AppointmentBuilder {
    private Long id;
    private PatientProfile patient;
    private DoctorProfile doctor;
    private LocalDateTime appointmentTime;
    private AppointmentStatus status;

    public AppointmentBuilder id(Long id) { this.id = id; return this; }
    public AppointmentBuilder patient(PatientProfile patient) { this.patient = patient; return this; }
    public AppointmentBuilder doctor(DoctorProfile doctor) { this.doctor = doctor; return this; }
    public AppointmentBuilder appointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; return this; }
    public AppointmentBuilder status(AppointmentStatus status) { this.status = status; return this; }

    public Appointment build() {
        return new Appointment(id, patient, doctor, appointmentTime, status, appointmentTime, null);
    }
}

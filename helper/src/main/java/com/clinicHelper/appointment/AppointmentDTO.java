package com.clinicHelper.appointment;

import java.time.LocalDateTime;

import com.clinicHelper.doctor.Clinic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {
    private Long appointmentId;  // Changed from id to appointmentId
    private LocalDateTime appointmentTime;
    private AppointmentStatus status;
    private Integer patientId;
    private String patientName;
    private Integer doctorId;
    private String doctorName;
    private Clinic clinic;
    private LocalDateTime createdAt;

    public static AppointmentDTO fromEntity(Appointment appointment) {
        return AppointmentDTO.builder()
                .appointmentId(appointment.getAppointmentId())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus())
                .patientId(appointment.getPatient() != null ? appointment.getPatient().getUserId() : null)
                .patientName(appointment.getPatient() != null && appointment.getPatient().getUser() != null 
                    ? appointment.getPatient().getUser().getName() : null)
                .doctorId(appointment.getDoctor() != null ? appointment.getDoctor().getUserId() : null)
                .doctorName(appointment.getDoctor() != null && appointment.getDoctor().getUser() != null 
                    ? appointment.getDoctor().getUser().getName() : null)
                .clinic(appointment.getClinic())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
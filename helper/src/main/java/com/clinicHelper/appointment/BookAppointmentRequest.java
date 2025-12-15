package com.clinicHelper.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookAppointmentRequest {
    @NotNull(message = "Doctor ID is required")
    private Integer doctorId;
    
    @NotNull(message = "Clinic ID is required")
    private Integer clinicId;
    
    @NotNull(message = "Appointment time is required")
    @Future(message = "Appointment time must be in the future")
    private LocalDateTime appointmentTime;
    
    private String notes;
}


// can u gave me front end for this project useing html, css, javascript only not frame work just frontend to test is all functions work correctly
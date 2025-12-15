package com.clinicHelper.appointment;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookAppointmentResponse {
    private Long appointmentId;
    private String message;
    private LocalDateTime appointmentTime;
    private String doctorName;
    private String clinicName;
    private String status;
}

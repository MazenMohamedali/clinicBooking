package com.clinicHelper.HomeDashboard.DTOs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotsDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer clinicId;
    private String clinicName;
    private Integer doctorId;
    private String doctorName;
}

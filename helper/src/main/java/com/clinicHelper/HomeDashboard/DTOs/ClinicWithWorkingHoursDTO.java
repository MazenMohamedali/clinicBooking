package com.clinicHelper.HomeDashboard.DTOs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicWithWorkingHoursDTO {
    private Integer clinicId;
    private String name;
    private String phone;
    private String address;
    private List<WorkingHoursDTO> workingHours;
}

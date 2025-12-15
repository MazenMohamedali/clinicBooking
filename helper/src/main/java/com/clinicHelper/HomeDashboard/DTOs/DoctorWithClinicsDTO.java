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
public class DoctorWithClinicsDTO {
    private Integer doctorId;
    private String name;
    private String email;
    private String phone;
    private String specialization;
    private String bio;
    private List<ClinicWithWorkingHoursDTO> clinics;
}

package com.clinicHelper.HomeDashboard.DTOs;

import java.sql.Time;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHoursDTO {
    private Integer id;
    private Integer dayOfWeek;
    private Time startTime;
    private Time endTime;
}

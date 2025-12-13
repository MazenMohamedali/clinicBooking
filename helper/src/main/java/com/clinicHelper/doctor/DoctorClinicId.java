package com.clinicHelper.doctor;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorClinicId implements Serializable{
    private Integer doctorId;
    private Integer clinicId;
}

package com.clinicHelper.auth.RegisterRequest;

import java.time.LocalTime;
import java.util.HashMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class DoctorRegisterRequest extends BaseReqisterRequest {

    @NotBlank 
    private String speciality;
    private String bio;
    private byte[] img;

    @NotBlank 
    private String clinicName;
    private String clinicPhone;
    private String clinicAddress;

    // 0=Sunday, 1=Monday ... 6=Saturday
    private int startDayOfWork; 
    private int endDayOfWork;


    @NotNull
    private LocalTime startTime; 
    @NotNull
    private LocalTime endTime;
}

package com.clinicHelper.auth.RegisterRequest;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class patientRegisterRequest extends BaseReqisterRequest {
    @NotNull 
    private LocalDate birthDate;
    private String gender;
    private String insurance_number;
}
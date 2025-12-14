package com.clinicHelper.auth.RegisterRequest;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReceptionistRegisterRequest extends BaseReqisterRequest {
    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    private String notes;

    @NotEmpty(message = "At least one clinic must be assigned")
    private List<Integer> clinicIds;
}

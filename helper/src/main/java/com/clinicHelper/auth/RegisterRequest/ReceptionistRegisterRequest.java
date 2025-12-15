package com.clinicHelper.auth.RegisterRequest;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReceptionistRegisterRequest{
    @NotBlank
    private String name;

    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String phone;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    private String notes;

    @NotEmpty(message = "At least one clinic must be assigned")
    private List<Integer> clinicIds;
}

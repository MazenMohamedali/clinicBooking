package com.clinicHelper.Receptionist;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionistClinicId implements Serializable {
    private Integer receptionistId;
    private Integer clinicId;
}

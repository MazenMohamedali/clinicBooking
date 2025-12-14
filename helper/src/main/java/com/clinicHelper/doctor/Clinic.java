package com.clinicHelper.doctor;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "clinic")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Clinic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer clinicId;
    private String name;
    private String phone;
    private String address;
}
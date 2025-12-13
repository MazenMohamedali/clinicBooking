package com.clinicHelper.doctor;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "doctor_working_time")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DoctorWorkingTime {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "doctor_id")
    private Integer doctorId;

    @Column(name = "clinic_id")
    private Integer clinicId;

    private Integer dayOfWeek;

    private java.sql.Time startTime;
    private java.sql.Time endTime;
}
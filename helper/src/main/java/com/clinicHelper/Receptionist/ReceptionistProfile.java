package com.clinicHelper.Receptionist;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.clinicHelper.user.User;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "receptionist_profile")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionistProfile {
    @Id
    @Column(name = "user_id")
    private Integer userId;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    private String notes;
    
    @OneToMany(mappedBy = "receptionist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReceptionistClinic> clinicAssignments = new ArrayList<>();
}

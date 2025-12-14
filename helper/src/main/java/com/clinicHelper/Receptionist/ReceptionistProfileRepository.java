package com.clinicHelper.Receptionist;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceptionistProfileRepository extends JpaRepository<ReceptionistProfile, Integer> {
}
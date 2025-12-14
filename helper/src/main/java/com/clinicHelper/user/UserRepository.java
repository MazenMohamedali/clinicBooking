package com.clinicHelper.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Integer id);
    @Procedure(name = "delete_doctor_and_related")
    void deleteDoctorAndRelated(Integer in_user_id);

    @Procedure(name = "delete_patient_and_related")
    void deletePatientAndRelated(Integer in_user_id);

    @Procedure(name = "delete_receptionist_and_related")
    void deleteReceptionestAndRelated(Integer in_user_id);

}
package com.clinicHelper.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinicHelper.Role;

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

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);

    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC LIMIT 10")
    List<User> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();
}

package com.clinicHelper.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinicHelper.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final String ADMIN_EMAIL = "mazen@gmail.com";
    
    @Transactional
    public void deleteByUserEmail(String email) {
        User caller = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        deleteUserByProcedure(caller, caller.getId(), true);
    }

    @Transactional
    public void deleteById(int id) {
            User caller = userRepository.findByEmail(ADMIN_EMAIL)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        deleteUserByProcedure(caller, id, false);
    }

    private void deleteUserByProcedure(User caller, int deletedUserId, boolean selfDelete) {
        User target = userRepository.findById(deletedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        Role role = target.getRole();
        if(role == Role.DOCTOR) callDeleteDoctorProc(deletedUserId);
        else if(role == Role.PATIENT) callDeletePatientProc(deletedUserId);
        else if(role == Role.RECEPTIONIST) callDeleteRecptionestProc(deletedUserId);
        else throw new IllegalArgumentException("Cannot delete admin");

        // If you want app-level cleanup only after DB commit, you could publish an event and
        // handle cleanup in an @TransactionalEventListener(afterCommit = true).
        // refreshTokenRepository.deleteByUserId(targetUserId);
        // publish an event for audit/notifications
        // applicationEventPublisher.publishEvent(new UserDeletedEvent(targetUserId, caller.getId()))
    }

    private void callDeleteDoctorProc(int userId) {
        jdbcTemplate.update("CALL delete_doctor_and_related(?)", userId);
    }

    private void callDeletePatientProc(int userId) {
        jdbcTemplate.update("CALL delete_patient_and_related(?)", userId);
    }

    private void callDeleteRecptionestProc(int userId) {
        jdbcTemplate.update("CALL delete_receptionist_and_related(?)", userId);
    }
}
package com.clinicHelper.auth;

import java.sql.Time;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinicHelper.Role;
import com.clinicHelper.Config.JwtService;
import com.clinicHelper.auth.RegisterRequest.BaseReqisterRequest;
import com.clinicHelper.auth.RegisterRequest.DoctorRegisterRequest;
import com.clinicHelper.auth.RegisterRequest.patientRegisterRequest;
import com.clinicHelper.patient.PatientProfile;
import com.clinicHelper.patient.PatientProfileRepository;
import com.clinicHelper.user.User;
import com.clinicHelper.user.UserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JdbcTemplate jdbcTemplate;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = repository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(null, user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public AuthenticationResponse registerUser(BaseReqisterRequest register) {
        var user = User.builder()
            .name(register.getName())
            .email(register.getEmail())
            .hashedPassword(passwordEncoder.encode(register.getPassword()))
            .role(Role.ADMIN)
            .phone(register.getPhone())
            .build();
        repository.save(user);
        var jwtToken = jwtService.generateToken(null, user);
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build();
    }

    public AuthenticationResponse registerDoctor(DoctorRegisterRequest register) {
        if(repository.findByEmail(register.getEmail()).isPresent())
                throw new IllegalArgumentException("Email already registered");
            
        String hashed = passwordEncoder.encode(register.getPassword());
        try {
                Time startSql = Time.valueOf(register.getStartTime());
                Time endSql = Time.valueOf(register.getEndTime());

                jdbcTemplate.update(
                    "CALL RegisterClinicAndDoctor(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    register.getClinicName(),
                    register.getClinicPhone(),
                    register.getClinicAddress(),
                    register.getName(),
                    register.getEmail(),
                    hashed,
                    register.getPhone(),
                    register.getSpeciality(),
                    register.getBio(),
                    register.getImg(),                 
                    register.getStartDayOfWork(),
                    register.getEndDayOfWork(),
                    startSql,
                    endSql
                );
        } catch(DataAccessException ex) {
                throw new RuntimeException("Failed to register doctor in DB: " + ex.getMessage(), ex);
        }

            var user = repository.findByEmail(register.getEmail())
                    .orElseThrow(() -> new IllegalStateException("User created but not found"));

            user.setRole(Role.DOCTOR);
            repository.save(user);
            var jwtToken = jwtService.generateToken(null, user);
                return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
        }

    public AuthenticationResponse registerPationt(patientRegisterRequest register) {
        if(repository.findByEmail(register.getEmail()).isPresent()) throw new IllegalArgumentException("Email already registered");
        var user = User.builder()
            .name(register.getName())
            .email(register.getEmail())
            .hashedPassword(passwordEncoder.encode(register.getPassword()))
            .role(Role.PATIENT)
            .phone(register.getPhone())
            .build();

        var savedUser = repository.save(user);
        var patientProfile = PatientProfile.builder()
            .user(savedUser)
            .birthDate(register.getBirthDate())
            .insuranceNumber(register.getInsurance_number())
            .notes("New patient registered") 
            .build();

        patientProfileRepository.save(patientProfile);
        var jwtToken = jwtService.generateToken(null, savedUser);
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build();
    }
}
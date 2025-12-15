package com.clinicHelper.auth;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinicHelper.Role;
import com.clinicHelper.Config.JwtService;
import com.clinicHelper.Receptionist.ReceptionistClinic;
import com.clinicHelper.Receptionist.ReceptionistClinicId;
import com.clinicHelper.Receptionist.ReceptionistClinicRepository;
import com.clinicHelper.Receptionist.ReceptionistProfile;
import com.clinicHelper.Receptionist.ReceptionistProfileRepository;
import com.clinicHelper.auth.RegisterRequest.BaseReqisterRequest;
import com.clinicHelper.auth.RegisterRequest.DoctorRegisterRequest;
import com.clinicHelper.auth.RegisterRequest.ReceptionistRegisterRequest;
import com.clinicHelper.auth.RegisterRequest.patientRegisterRequest;
import com.clinicHelper.doctor.Clinic;
import com.clinicHelper.doctor.ClinicRepository;
import com.clinicHelper.doctor.DoctorClinic;
import com.clinicHelper.doctor.DoctorClinicId;
import com.clinicHelper.doctor.DoctorClinicRepository;
import com.clinicHelper.patient.PatientProfile;
import com.clinicHelper.patient.PatientProfileRepository;
import com.clinicHelper.user.User;
import com.clinicHelper.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PatientProfileRepository patientProfileRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorClinicRepository doctorClinicRepository;
    private final ReceptionistProfileRepository receptionistProfileRepository;
    private final ReceptionistClinicRepository receptionistClinicRepository;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JdbcTemplate jdbcTemplate;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticateRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticateRequest.getEmail(),
                        authenticateRequest.getPassword()
                )
        );
        var user = repository.findByEmail(authenticateRequest.getEmail())
                .orElseThrow();

        System.out.println("=== DEBUG AUTHENTICATION ===");
        System.out.println("User Email: " + user.getEmail());
        System.out.println("User Role: " + user.getRole());

        String roleName = null;
        String userName = null;

        if (user.getRole() != null) {
            roleName = user.getRole().name();
        }

        if (user.getName() != null) {
            userName = user.getName();
        }

        var jwtToken = jwtService.generateToken(null, user);

        Long doctorClinicId = null;
        if (user.getRole() == Role.DOCTOR) {
            var clinicLink = doctorClinicRepository.findFirstByDoctor(user);

            if (clinicLink.isPresent()) {
                doctorClinicId = clinicLink.get().getClinic().getClinicId().longValue();
                System.out.println("Doctor Clinic ID Found: " + doctorClinicId);
            }
        }

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(jwtToken)
                .role(roleName)
                .name(userName)
                .clinicId(doctorClinicId)
                .build();

        System.out.println("Response Token: " + (response.getToken() != null ? "EXISTS" : "NULL"));
        System.out.println("Response Role: " + response.getRole());
        System.out.println("Response Name: " + response.getName());
        System.out.println("Response ClinicID: " + response.getClinicId());
        System.out.println("=== END DEBUG ===");

        return response;
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
                .role(user.getRole().name())
                .name(user.getName())
                .build();
    }

    @Transactional
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

            System.out.println("Stored Procedure executed successfully");

        } catch(DataAccessException ex) {
            throw new RuntimeException("Failed to register doctor in DB: " + ex.getMessage(), ex);
        }

        var user = repository.findByEmail(register.getEmail())
                .orElseThrow(() -> new IllegalStateException("User created but not found"));

        user.setRole(Role.DOCTOR);
        repository.save(user);

        System.out.println("User found with ID: " + user.getId());

        Clinic clinic = findClinicByPhone(register.getClinicPhone());

        if (clinic == null) {
            throw new IllegalStateException(
                    "Clinic was not created properly. Phone: " + register.getClinicPhone()
            );
        }

        System.out.println("Clinic found with ID: " + clinic.getClinicId());

        createDoctorClinicLink(user, clinic);

        System.out.println("DoctorClinic link created successfully");

        var jwtToken = jwtService.generateToken(null, user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(user.getRole().name())
                .name(user.getName())
                .clinicId(clinic.getClinicId().longValue())
                .build();
    }

    // 4. تسجيل مريض
    public AuthenticationResponse registerPationt(patientRegisterRequest register) {
        if(repository.findByEmail(register.getEmail()).isPresent())
            throw new IllegalArgumentException("Email already registered");

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
                .role(savedUser.getRole().name())
                .name(savedUser.getName())
                .build();
    }

    public AuthenticationResponse registerReceptionist(ReceptionistRegisterRequest request, String doctorEmail) {
        User doctor = repository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        List<Clinic> clinics = validateClinicsAndOwnership(request.getClinicIds(), doctor);
        User receptionist = createReceptionistUser(request);
        User savedReceptionist = repository.save(receptionist);
        ReceptionistProfile profile = createReceptionistProfile(request, savedReceptionist);
        ReceptionistProfile savedProfile = receptionistProfileRepository.save(profile);
        createClinicAssignments(savedReceptionist, clinics, doctor, savedProfile);
        String jwtToken = jwtService.generateToken(null, savedReceptionist);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(savedReceptionist.getRole().name())
                .name(savedReceptionist.getName())
                .build();
    }

    private Clinic findClinicByPhone(String phone) {
        List<Clinic> allClinics = clinicRepository.findAll();

        for (Clinic clinic : allClinics) {
            if (clinic.getPhone() != null && clinic.getPhone().equals(phone)) {
                return clinic;
            }
        }

        return null;
    }

    private void createDoctorClinicLink(User doctor, Clinic clinic) {
        boolean alreadyExists = doctorClinicRepository.existsByDoctorAndClinic(doctor, clinic);

        if (alreadyExists) {
            System.out.println("DoctorClinic link already exists, skipping creation");
            return;
        }

        DoctorClinicId doctorClinicId = DoctorClinicId.builder()
                .doctorId(doctor.getId())
                .clinicId(clinic.getClinicId())
                .build();

        DoctorClinic doctorClinic = DoctorClinic.builder()
                .id(doctorClinicId)
                .doctor(doctor)
                .clinic(clinic)
                .build();

        doctorClinicRepository.save(doctorClinic);

        System.out.println("DoctorClinic saved: Doctor ID = " + doctor.getId() +
                ", Clinic ID = " + clinic.getClinicId());
        }

    private List<Clinic> validateClinicsAndOwnership(List<Integer> clinicIds, User doctor) {
        List<Clinic> clinics = new ArrayList<>();
        for (Integer clinicId : clinicIds) {
            Clinic clinic = clinicRepository.findById(clinicId)
                    .orElseThrow(() -> new IllegalArgumentException("Clinic not found with ID: " + clinicId));
            boolean doctorOwnsClinic = doctorClinicRepository.existsByDoctorAndClinic(doctor, clinic);
            if (!doctorOwnsClinic) {
                throw new SecurityException("Doctor doesn't own clinic ID: " + clinicId + ". Only clinic owners can assign receptionists.");
            }
            clinics.add(clinic);
        }
        return clinics;
    }

    private User createReceptionistUser(ReceptionistRegisterRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .hashedPassword(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.RECEPTIONIST)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ReceptionistProfile createReceptionistProfile(ReceptionistRegisterRequest request, User receptionist) {
        return ReceptionistProfile.builder()
                .user(receptionist)
                .hireDate(request.getHireDate())
                .notes(request.getNotes())
                .build();
    }

    private void createClinicAssignments(User receptionist, List<Clinic> clinics, User doctor, ReceptionistProfile profile) {
        List<ReceptionistClinic> assignments = new ArrayList<>();
        for (Clinic clinic : clinics) {
            ReceptionistClinic assignment = ReceptionistClinic.builder()
                    .id(new ReceptionistClinicId(receptionist.getId(), clinic.getClinicId()))
                    .receptionist(profile)
                    .clinic(clinic)
                    .assignedByDoctor(doctor)
                    .build();
            assignments.add(assignment);
        }

        receptionistClinicRepository.saveAll(assignments);

        if (profile.getClinicAssignments() == null) {
            profile.setClinicAssignments(new ArrayList<>());
        }

        profile.getClinicAssignments().addAll(assignments);

        receptionistProfileRepository.save(profile);
    }
}
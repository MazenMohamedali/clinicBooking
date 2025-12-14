package com.clinicHelper.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicHelper.Role;
import com.clinicHelper.auth.RegisterRequest.BaseReqisterRequest;
import com.clinicHelper.auth.RegisterRequest.DoctorRegisterRequest;
import com.clinicHelper.auth.RegisterRequest.patientRegisterRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    @PostMapping("/register/admin")
    public ResponseEntity<AuthenticationResponse> registerAdmin(@RequestBody BaseReqisterRequest register) {
        return ResponseEntity.ok(authenticationService.registerUser(register));
    }
    
    @PostMapping("/register/patient")
    public ResponseEntity<AuthenticationResponse> registerPatient(@RequestBody patientRegisterRequest register) {
        return ResponseEntity.ok(authenticationService.registerPationt(register));
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<AuthenticationResponse> registerDoctor(@RequestBody DoctorRegisterRequest register) {
        return ResponseEntity.ok(authenticationService.registerDoctor(register));
    }
    
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}

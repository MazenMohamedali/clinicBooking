package com.clinicHelper.doctor;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;                 // correct import
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicHelper.auth.AuthenticationResponse;
import com.clinicHelper.auth.AuthenticationService;
import com.clinicHelper.auth.RegisterRequest.ReceptionistRegisterRequest;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/doctor")
public class DoctorController {
    private final AuthenticationService receptionistRegistrationService;
    // private final UserRepository userRepository;
    @PostMapping("/register/receptionist")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AuthenticationResponse> registerReceptionist( @Valid @RequestBody ReceptionistRegisterRequest request, Principal principal) {
        AuthenticationResponse response = receptionistRegistrationService
        .registerReceptionist(request, principal.getName());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("DEBUG AUTH: principal=" + auth.getPrincipal() + " authorities=" + auth.getAuthorities());
        return ResponseEntity.ok(response);
    }

    // @PreAuthorize("hasRole('DOCTOR')")
    // @GetMapping("/receptionists")
    // public ResponseEntity<List<ReceptionistDTO>> getMyReceptionists(Principal principal) {
    //     User doctor = userRepository.findByEmail(principal.getName())
    //             .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));
        
    //     List<ReceptionistDTO> receptionists = receptionistRegistrationService
    //             .getReceptionistsByDoctor(doctor);
        
    //     return ResponseEntity.ok(receptionists);
    // }

    // @PreAuthorize("hasRole('DOCTOR')")
    // @GetMapping("/dashboard")
    // public ResponseEntity<String> doctorDashboard(Principal principal) {
    //     User doctor = userRepository.findByEmail(principal.getName())
    //             .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));
        
    //     return ResponseEntity.ok("Welcome to Doctor Dashboard, " + doctor.getName());
    // }
}
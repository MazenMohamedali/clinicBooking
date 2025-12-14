package com.clinicHelper.doctor;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicHelper.auth.AuthenticationResponse;
import com.clinicHelper.auth.AuthenticationService;
import com.clinicHelper.auth.RegisterRequest.ReceptionistRegisterRequest;
import com.clinicHelper.user.UserRepository;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/doctor")
public class DoctorController {
    private final AuthenticationService receptionistRegistrationService;
    // private final UserRepository userRepository;

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/register/receptionist")
    public ResponseEntity<AuthenticationResponse> registerReceptionist(
            @Valid @RequestBody ReceptionistRegisterRequest request,
            Principal principal
    ) {
        AuthenticationResponse response = receptionistRegistrationService
                .registerReceptionist(request, principal.getName());
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
package com.clinicHelper.Admin;

import com.clinicHelper.user.User;
import com.clinicHelper.user.UserRepository;
import com.clinicHelper.Role;
import com.clinicHelper.appointment.Appointment;
import com.clinicHelper.appointment.AppointmentRepository;
import com.clinicHelper.doctor.Clinic;
import com.clinicHelper.doctor.ClinicRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {
    
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClinicRepository clinicRepository;
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(Principal principal) {
        User admin = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        long totalUsers = userRepository.count();
        long totalDoctors = userRepository.countByRole(Role.DOCTOR);
        long totalPatients = userRepository.countByRole(Role.PATIENT);
        long totalReceptionists = userRepository.countByRole(Role.RECEPTIONIST);
        long totalAppointments = appointmentRepository.count();
        long totalClinics = clinicRepository.count();
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("adminName", admin.getName());
        dashboard.put("adminEmail", admin.getEmail());
        dashboard.put("stats", Map.of(
            "totalUsers", totalUsers,
            "totalDoctors", totalDoctors,
            "totalPatients", totalPatients,
            "totalReceptionists", totalReceptionists,
            "totalAppointments", totalAppointments,
            "totalClinics", totalClinics
        ));
        
        List<User> recentUsers = userRepository.findTop10ByOrderByCreatedAtDesc();
        dashboard.put("recentUsers", recentUsers);
        
        List<Appointment> recentAppointments = appointmentRepository.findTop10ByOrderByCreatedAtDesc();
        dashboard.put("recentAppointments", recentAppointments);
        
        return ResponseEntity.ok(dashboard);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentRepository.findAll());
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/clinics")
    public ResponseEntity<List<Clinic>> getAllClinics() {
        return ResponseEntity.ok(clinicRepository.findAll());
    }
}
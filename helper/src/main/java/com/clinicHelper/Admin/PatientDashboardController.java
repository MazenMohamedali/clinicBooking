package com.clinicHelper.Admin;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicHelper.appointment.Appointment;
import com.clinicHelper.appointment.AppointmentRepository;
import com.clinicHelper.appointment.AppointmentService;
import com.clinicHelper.appointment.AppointmentStatus;
import com.clinicHelper.user.UserRepository;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientDashboardController {
    
    private final UserRepository userRepository;
    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(Principal principal) {
        User patient = (User) userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
        
        long upcomingCount = appointments.stream()
                .filter(a -> a.getAppointmentTime().isAfter(LocalDateTime.now()) 
                          && a.getStatus() == AppointmentStatus.SCHEDULED)
                .count();
        
        long completedCount = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();
        
        long cancelledCount = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED)
                .count();
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("patientName", patient.getName());
        dashboard.put("patientEmail", ((com.clinicHelper.user.User) patient).getEmail());
        dashboard.put("stats", Map.of(
            "totalAppointments", appointments.size(),
            "upcomingAppointments", upcomingCount,
            "completedAppointments", completedCount,
            "cancelledAppointments", cancelledCount
        ));
        
        List<Appointment> upcomingAppointments = appointments.stream()
                .filter(a -> a.getAppointmentTime().isAfter(LocalDateTime.now())
                          && a.getAppointmentTime().isBefore(LocalDateTime.now().plusDays(7))
                          && a.getStatus() == AppointmentStatus.SCHEDULED)
                .sorted((a1, a2) -> a1.getAppointmentTime().compareTo(a2.getAppointmentTime()))
                .toList();
        
        dashboard.put("upcomingAppointments", upcomingAppointments);
        
        return ResponseEntity.ok(dashboard);
    }
    
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getMyAppointments(Principal principal) {
        User patient = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
        return ResponseEntity.ok(appointments);
    }
    
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/appointments/upcoming")
    public ResponseEntity<List<Appointment>> getUpcomingAppointments(Principal principal) {
        User patient = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
        List<Appointment> upcoming = appointments.stream()
                .filter(a -> a.getAppointmentTime().isAfter(LocalDateTime.now())
                          && a.getStatus() == AppointmentStatus.SCHEDULED)
                .sorted((a1, a2) -> a1.getAppointmentTime().compareTo(a2.getAppointmentTime()))
                .toList();
        
        return ResponseEntity.ok(upcoming);
    }
    
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/appointments/cancel/{appointmentId}")
    public ResponseEntity<Appointment> cancelAppointment(
            @PathVariable Long appointmentId,
            Principal principal) {
        
        User patient = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new SecurityException("You can only cancel your own appointments");
        }
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment cancelled = appointmentRepository.save(appointment);
        
        return ResponseEntity.ok(cancelled);
    }
}

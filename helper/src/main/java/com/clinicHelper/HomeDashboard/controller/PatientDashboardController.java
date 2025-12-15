package com.clinicHelper.HomeDashboard.controller;

import com.clinicHelper.HomeDashboard.DTOs.AvailableSlotsDTO;
import com.clinicHelper.HomeDashboard.DTOs.ClinicWithWorkingHoursDTO;
import com.clinicHelper.HomeDashboard.DTOs.DoctorWithClinicsDTO;
import com.clinicHelper.HomeDashboard.PatientDashBoard.PatientDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientDashboardController {

    private final PatientDashboardService patientDashboardService;

    @GetMapping("/doctors")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<List<DoctorWithClinicsDTO>> getAllDoctorsWithClinics() {
        List<DoctorWithClinicsDTO> doctors = patientDashboardService.getAllDoctorsWithClinics();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/clinics")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<List<ClinicWithWorkingHoursDTO>> getAllClinics() {
        List<ClinicWithWorkingHoursDTO> clinics = patientDashboardService.getAllClinicsWithDoctors();
        return ResponseEntity.ok(clinics);
    }

    @GetMapping("/doctors/{doctorId}/available-slots")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<List<AvailableSlotsDTO>> getAvailableSlots(
            @PathVariable Integer doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<AvailableSlotsDTO> availableSlots = patientDashboardService.getAvailableAppointmentSlots(doctorId, date);
        return ResponseEntity.ok(availableSlots);
    }

    @GetMapping("/doctors/search")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<List<DoctorWithClinicsDTO>> searchDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String name) {
        
        List<DoctorWithClinicsDTO> allDoctors = patientDashboardService.getAllDoctorsWithClinics();
        
        List<DoctorWithClinicsDTO> filtered = allDoctors.stream()
                .filter(doctor -> 
                    (specialization == null || doctor.getSpecialization().toLowerCase().contains(specialization.toLowerCase())) &&
                    (name == null || doctor.getName().toLowerCase().contains(name.toLowerCase()))
                )
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/doctors/{doctorId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<DoctorWithClinicsDTO> getDoctorById(@PathVariable Integer doctorId) {
        List<DoctorWithClinicsDTO> allDoctors = patientDashboardService.getAllDoctorsWithClinics();
        
        return allDoctors.stream()
                .filter(doctor -> doctor.getDoctorId().equals(doctorId))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
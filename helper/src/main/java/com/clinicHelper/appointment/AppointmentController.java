package com.clinicHelper.appointment;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clinicHelper.doctor.Clinic;
import com.clinicHelper.doctor.ClinicRepository;
import com.clinicHelper.doctor.DoctorProfile;
import com.clinicHelper.doctor.DoctorProfileRepository;
import com.clinicHelper.exceptions.ApiException;
import com.clinicHelper.patient.PatientProfile;
import com.clinicHelper.patient.PatientProfileRepository;
import com.clinicHelper.user.User;
import com.clinicHelper.user.UserRepository;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Builder
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final ClinicRepository clinicRepository;

    @PostMapping("/book")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<BookAppointmentResponse> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request,
            Principal principal) {
        
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        
        PatientProfile patient = patientProfileRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ApiException("Patient profile not found", HttpStatus.NOT_FOUND));
        
        DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ApiException("Doctor not found", HttpStatus.NOT_FOUND));
        
        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new ApiException("Clinic not found", HttpStatus.NOT_FOUND));
        
        Appointment appointment = appointmentService.bookAppointment(
                patient.getUserId(), 
                doctor.getUserId(), 
                request.getAppointmentTime());
        
        if (appointment.getClinic() == null) {
            appointment.setClinic(clinic);
            appointmentRepository.save(appointment);
        }
        
        BookAppointmentResponse response = BookAppointmentResponse.builder()
                .appointmentId(appointment.getAppointmentId())
                .message("Appointment booked successfully")
                .appointmentTime(appointment.getAppointmentTime())
                .doctorName(doctor.getUser().getName())
                .clinicName(clinic.getName())
                .status(appointment.getStatus().name())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/patient/my-appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentDTO>> getMyAppointments(Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        
        List<Appointment> appointments = appointmentService.getPatientAppointments(currentUser.getId());
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(AppointmentDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(appointmentDTOs);
    }


    @PutMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<AppointmentDTO> cancelAppointment(
            @PathVariable Long appointmentId,
            Principal principal) {
        
        if (principal != null) {
            User currentUser = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
            
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new ApiException("Appointment not found", HttpStatus.NOT_FOUND));
            
            if (currentUser.getRole().name().equals("PATIENT") && 
                !appointment.getPatient().getUserId().equals(currentUser.getId())) {
                throw new ApiException("You can only cancel your own appointments", HttpStatus.FORBIDDEN);
            }
            
            if (currentUser.getRole().name().equals("DOCTOR") && 
                !appointment.getDoctor().getUserId().equals(currentUser.getId())) {
                throw new ApiException("You can only cancel your own appointments", HttpStatus.FORBIDDEN);
            }
        }
        
        Appointment cancelledAppointment = appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.ok(AppointmentDTO.fromEntity(cancelledAppointment));
    }

    @GetMapping("/doctor/my-appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointments(Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        
        List<Appointment> appointments = appointmentService.getDoctorAppointments(currentUser.getId());
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(AppointmentDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/doctor/appointments-by-date")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointmentsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Principal principal) {
        
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        List<Appointment> appointments = appointmentRepository.findByDoctorId(currentUser.getId())
                .stream()
                .filter(appointment -> 
                    appointment.getAppointmentTime().isAfter(startOfDay) &&
                    appointment.getAppointmentTime().isBefore(endOfDay)
                )
                .collect(Collectors.toList());
        
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(AppointmentDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(appointmentDTOs);
    }

    @PutMapping("/{appointmentId}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentStatusRequest request,
            Principal principal) {
        
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException("Appointment not found", HttpStatus.NOT_FOUND));
        
        if (!appointment.getDoctor().getUserId().equals(currentUser.getId())) {
            throw new ApiException("You can only update your own appointments", HttpStatus.FORBIDDEN);
        }
        
        appointment.setStatus(request.getStatus());
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        return ResponseEntity.ok(AppointmentDTO.fromEntity(updatedAppointment));
    }

    @PostMapping("/receptionist/book")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<BookAppointmentResponse> bookAppointmentForPatient(
            @Valid @RequestBody BookAppointmentRequest request,
            @RequestParam Integer patientId,
            Principal principal) {
        
        PatientProfile patient = patientProfileRepository.findById(patientId)
                .orElseThrow(() -> new ApiException("Patient not found", HttpStatus.NOT_FOUND));
        
        DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ApiException("Doctor not found", HttpStatus.NOT_FOUND));
        
        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new ApiException("Clinic not found", HttpStatus.NOT_FOUND));
        
        Appointment appointment = appointmentService.bookAppointment(
                patient.getUserId(), 
                doctor.getUserId(), 
                request.getAppointmentTime());
        
        appointment.setClinic(clinic);
        appointmentRepository.save(appointment);
        
        BookAppointmentResponse response = BookAppointmentResponse.builder()
                .appointmentId(appointment.getAppointmentId())
                .message("Appointment booked successfully for patient")
                .appointmentTime(appointment.getAppointmentTime())
                .doctorName(doctor.getUser().getName())
                .clinicName(clinic.getName())
                .status(appointment.getStatus().name())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/receptionist/clinic/{clinicId}")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByClinic(
            @PathVariable Integer clinicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Principal principal) {
        
        List<Appointment> appointments;
        
        if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            appointments = appointmentRepository.findByAppointmentTimeBetween(startOfDay, endOfDay)
                    .stream()
                    .filter(appointment -> 
                        appointment.getClinic() != null && 
                        appointment.getClinic().getClinicId().equals(clinicId)
                    )
                    .collect(Collectors.toList());
        } else {
            appointments = appointmentRepository.findByClinicId(clinicId);
        }
        
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(AppointmentDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<AppointmentDTO>> getTodayAppointments(Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        List<Appointment> appointments;
        
        if (currentUser.getRole().name().equals("DOCTOR")) {
            // Doctor sees only their appointments
            appointments = appointmentRepository.findByDoctorId(currentUser.getId())
                    .stream()
                    .filter(appointment -> 
                        appointment.getAppointmentTime().isAfter(startOfDay) &&
                        appointment.getAppointmentTime().isBefore(endOfDay)
                    )
                    .collect(Collectors.toList());
        } else appointments = appointmentRepository.findByAppointmentTimeBetween(startOfDay, endOfDay);
        
        
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(AppointmentDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentDTO> getAppointmentById(
            @PathVariable Long appointmentId,
            Principal principal) {
        
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException("Appointment not found", HttpStatus.NOT_FOUND));
        
        // Check authorization
        if (!currentUser.getRole().name().equals("ADMIN") && 
            !currentUser.getRole().name().equals("RECEPTIONIST")) {
            
            if (currentUser.getRole().name().equals("PATIENT") && 
                !appointment.getPatient().getUserId().equals(currentUser.getId())) {
                throw new ApiException("You can only view your own appointments", HttpStatus.FORBIDDEN);
            }
            
            if (currentUser.getRole().name().equals("DOCTOR") && 
                !appointment.getDoctor().getUserId().equals(currentUser.getId())) {
                throw new ApiException("You can only view your own appointments", HttpStatus.FORBIDDEN);
            }
        }
        
        return ResponseEntity.ok(AppointmentDTO.fromEntity(appointment));
    }

    @PutMapping("/{appointmentId}/reschedule")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<AppointmentDTO> rescheduleAppointment(
            @PathVariable Long appointmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newTime,
            Principal principal) {
        
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException("Appointment not found", HttpStatus.NOT_FOUND));
        
        if (currentUser.getRole().name().equals("PATIENT") && 
            !appointment.getPatient().getUserId().equals(currentUser.getId())) {
            throw new ApiException("You can only reschedule your own appointments", HttpStatus.FORBIDDEN);
        }
        
        if (currentUser.getRole().name().equals("DOCTOR") && 
            !appointment.getDoctor().getUserId().equals(currentUser.getId())) {
            throw new ApiException("You can only reschedule your own appointments", HttpStatus.FORBIDDEN);
        }
        
        if (appointmentRepository.existsByDoctorIdAndAppointmentTime(
                appointment.getDoctor().getUserId(), newTime)) {
            throw new ApiException("Appointment slot is already taken", HttpStatus.CONFLICT);
        }
        
        appointment.setAppointmentTime(newTime);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        return ResponseEntity.ok(AppointmentDTO.fromEntity(updatedAppointment));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<?> getAppointmentStatistics(
            @RequestParam(required = false) Integer doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal) {
        
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        
        if (currentUser.getRole().name().equals("DOCTOR")) {
            doctorId = currentUser.getId();
        }
        
        List<Object[]> statusCounts = appointmentRepository.countAppointmentsByStatus();
        return ResponseEntity.ok(statusCounts);
    }
}
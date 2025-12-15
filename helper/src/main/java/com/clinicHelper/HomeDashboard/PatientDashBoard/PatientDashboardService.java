package com.clinicHelper.HomeDashboard.PatientDashBoard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicHelper.Role;
import com.clinicHelper.HomeDashboard.DTOs.AvailableSlotsDTO;
import com.clinicHelper.HomeDashboard.DTOs.ClinicWithWorkingHoursDTO;
import com.clinicHelper.HomeDashboard.DTOs.DoctorWithClinicsDTO;
import com.clinicHelper.HomeDashboard.DTOs.WorkingHoursDTO;
import com.clinicHelper.HomeDashboard.Repository.DoctorWorkingTimeRepository;
import com.clinicHelper.doctor.Clinic;
import com.clinicHelper.doctor.ClinicRepository;
import com.clinicHelper.doctor.DoctorProfile;
import com.clinicHelper.doctor.DoctorProfileRepository;
import com.clinicHelper.doctor.DoctorWorkingTime;
import com.clinicHelper.user.User;
import com.clinicHelper.user.UserRepository;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientDashboardService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorWorkingTimeRepository doctorWorkingTimeRepository;

    public List<DoctorWithClinicsDTO> getAllDoctorsWithClinics() {
        List<User> doctors = userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == Role.DOCTOR)
                .collect(Collectors.toList());

        List<DoctorWithClinicsDTO> result = new ArrayList<>();

        for (User doctor : doctors) {
            DoctorProfile doctorProfile = doctorProfileRepository.findById(doctor.getId())
                    .orElse(null);

            if (doctorProfile == null) continue;

            List<DoctorWorkingTime> workingTimes = doctorWorkingTimeRepository.findByDoctorId(doctor.getId());
            
            Map<Clinic, List<DoctorWorkingTime>> clinicWorkingMap = workingTimes.stream()
                    .collect(Collectors.groupingBy(wt -> clinicRepository.findById(wt.getClinicId()).orElse(null)));

            List<ClinicWithWorkingHoursDTO> clinics = new ArrayList<>();
            
            for (Map.Entry<Clinic, List<DoctorWorkingTime>> entry : clinicWorkingMap.entrySet()) {
                Clinic clinic = entry.getKey();
                if (clinic == null) continue;

                List<WorkingHoursDTO> workingHours = entry.getValue().stream()
                        .map(wt -> WorkingHoursDTO.builder()
                                .id(wt.getId())
                                .dayOfWeek(wt.getDayOfWeek())
                                .startTime(wt.getStartTime())
                                .endTime(wt.getEndTime())
                                .build())
                        .collect(Collectors.toList());

                clinics.add(ClinicWithWorkingHoursDTO.builder()
                        .clinicId(clinic.getClinicId())
                        .name(clinic.getName())
                        .phone(clinic.getPhone())
                        .address(clinic.getAddress())
                        .workingHours(workingHours)
                        .build());
            }

            result.add(DoctorWithClinicsDTO.builder()
                    .doctorId(doctor.getId())
                    .name(doctor.getName())
                    .email(doctor.getEmail())
                    .phone(doctor.getPhone())
                    .specialization(doctorProfile.getSpecialization())
                    .bio(doctorProfile.getBio())
                    .clinics(clinics)
                    .build());
        }

        return result;
    }

    public List<ClinicWithWorkingHoursDTO> getAllClinicsWithDoctors() {
        List<Clinic> clinics = clinicRepository.findAll();
        List<ClinicWithWorkingHoursDTO> result = new ArrayList<>();

        for (Clinic clinic : clinics) {
            List<DoctorWorkingTime> workingTimes = doctorWorkingTimeRepository.findByClinicId(clinic.getClinicId());
            
            Map<Integer, List<DoctorWorkingTime>> doctorWorkingMap = workingTimes.stream()
                    .collect(Collectors.groupingBy(DoctorWorkingTime::getDoctorId));

            List<WorkingHoursDTO> aggregatedHours = new ArrayList<>();
            
            for (List<DoctorWorkingTime> doctorTimes : doctorWorkingMap.values()) {
                aggregatedHours.addAll(doctorTimes.stream()
                        .map(wt -> WorkingHoursDTO.builder()
                                .id(wt.getId())
                                .dayOfWeek(wt.getDayOfWeek())
                                .startTime(wt.getStartTime())
                                .endTime(wt.getEndTime())
                                .build())
                        .collect(Collectors.toList()));
            }

            result.add(ClinicWithWorkingHoursDTO.builder()
                    .clinicId(clinic.getClinicId())
                    .name(clinic.getName())
                    .phone(clinic.getPhone())
                    .address(clinic.getAddress())
                    .workingHours(aggregatedHours)
                    .build());
        }

        return result;
    }

    public List<AvailableSlotsDTO> getAvailableAppointmentSlots(Integer doctorId, LocalDate date) {
        List<AvailableSlotsDTO> availableSlots = new ArrayList<>();
        
        int dayOfWeek = date.getDayOfWeek().getValue() % 7;
        List<DoctorWorkingTime> workingTimes = doctorWorkingTimeRepository
                .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);

        for (DoctorWorkingTime wt : workingTimes) {
            Clinic clinic = clinicRepository.findById(wt.getClinicId()).orElse(null);
            if (clinic == null) continue;

            LocalTime start = wt.getStartTime().toLocalTime();
            LocalTime end = wt.getEndTime().toLocalTime();
            
            while (start.isBefore(end)) {
                LocalTime slotEnd = start.plusMinutes(30);
                if (slotEnd.isAfter(end)) break;

                LocalDateTime slotStartDateTime = LocalDateTime.of(date, start);
                LocalDateTime slotEndDateTime = LocalDateTime.of(date, slotEnd);

                availableSlots.add(AvailableSlotsDTO.builder()
                        .startTime(slotStartDateTime)
                        .endTime(slotEndDateTime)
                        .clinicId(clinic.getClinicId())
                        .clinicName(clinic.getName())
                        .doctorId(doctorId)
                        .doctorName(getDoctorName(doctorId))
                        .build());

                start = slotEnd;
            }
        }

        return availableSlots;
    }

    private String getDoctorName(Integer doctorId) {
        return userRepository.findById(doctorId)
                .map(User::getName)
                .orElse("Unknown Doctor");
    }
}

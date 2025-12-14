// package com.clinicHelper.Admin;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.HttpStatusCode;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.clinicHelper.doctor.*;

// import lombok.RequiredArgsConstructor;
// @RestController
// @RequiredArgsConstructor
// @RequestMapping("/admin")
// public class AdminController {

//     @Autowired
//     private final Clinic clinicService;

//     @PreAuthorize("hasRole('ADMIN')")
//     @GetMapping("/dashboard")
//     public ResponseEntity<List<DoctorClinic>> adminDashboard() {
//         List<DoctorClinic> list = clinicService.getAllClinicsWithDoctors();
//         return ResponseEntity.ok(list);
//     }

//     @PreAuthorize("hasRole('ADMIN')")
//     @DeleteMapping("/clinics/{id}")
//     public ResponseEntity<Void> deleteClinic(@PathVariable("id") int clinicId) {
//         clinicService.deleteClinic(clinicId);
//         return ResponseEntity.noContent().build();
//     }

//     @PreAuthorize("hasRole('ADMIN')")
//     @PutMapping("/clinics/{id}")
//     public ResponseEntity<DoctorProfile> editClinic(
//             @PathVariable("id") int clinicId,
//             @RequestBody DoctorProfile updateData) {

//         DoctorProfile updated = clinicService.updateClinic(clinicId, updateData);
//         return ResponseEntity.ok(updated);
//     }

//     @PreAuthorize("hasRole('ADMIN')")
//     @PostMapping("/clinics")
//     public ResponseEntity<DoctorClinic> addClinic(@RequestBody DoctorProfile clinicWillBeAdded) {
//         DoctorClinic created = clinicService.addClinic(clinicWillBeAdded);
//         return ResponseEntity.status(HttpStatus.CREATED).body(created);
//     }
// }

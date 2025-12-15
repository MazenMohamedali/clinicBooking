package com.clinicHelper.HomeDashboard.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinicHelper.doctor.DoctorWorkingTime;

import java.util.List;

@Repository
public interface DoctorWorkingTimeRepository extends JpaRepository<DoctorWorkingTime, Integer> {
    
    List<DoctorWorkingTime> findByDoctorId(Integer doctorId);
    List<DoctorWorkingTime> findByClinicId(Integer clinicId);
    @Query("SELECT w FROM DoctorWorkingTime w WHERE w.doctorId = :doctorId AND w.dayOfWeek = :dayOfWeek")
    List<DoctorWorkingTime> findByDoctorIdAndDayOfWeek(@Param("doctorId") Integer doctorId, @Param("dayOfWeek") Integer dayOfWeek);
    @Query("SELECT w FROM DoctorWorkingTime w WHERE w.doctorId = :doctorId AND w.clinicId = :clinicId")
    List<DoctorWorkingTime> findByDoctorIdAndClinicId(@Param("doctorId") Integer doctorId, @Param("clinicId") Integer clinicId);
}
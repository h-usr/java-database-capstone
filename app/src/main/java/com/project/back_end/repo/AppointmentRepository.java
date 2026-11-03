package com.project.back_end.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Appointment.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT DISTINCT a FROM Appointment a "
       + "LEFT JOIN FETCH a.doctor d "
       + "LEFT JOIN FETCH d.availableTimes "
       + "WHERE d.id = :doctorId AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
       @Param("doctorId") Long doctorId,
       @Param("start") LocalDateTime start,
       @Param("end") LocalDateTime end);

    @Query("SELECT DISTINCT a FROM Appointment a "
       + "LEFT JOIN FETCH a.doctor "
       + "LEFT JOIN FETCH a.patient p "
       + "WHERE a.doctor.id = :doctorId "
       + "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%')) "
       + "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
       @Param("doctorId") Long doctorId,
       @Param("patientName") String patientName,
       @Param("start") LocalDateTime start,
       @Param("end") LocalDateTime end);

    @Modifying
    @Transactional
    void deleteAllByDoctorId(Long doctorId);

    List<Appointment> findByPatientId(Long patientId);

    default List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
       Long patientId, int status) {
   return findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, statusFromOrdinal(status));
    }

    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
       Long patientId, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a "
       + "LEFT JOIN a.doctor d "
       + "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) "
       + "AND a.patient.id = :patientId")
    List<Appointment> filterByDoctorNameAndPatientId(
       @Param("doctorName") String doctorName,
       @Param("patientId") Long patientId);

    default List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
       String doctorName, Long patientId, int status) {
   return filterByDoctorNameAndPatientIdAndStatus(
      doctorName, patientId, statusFromOrdinal(status));
    }

    @Query("SELECT a FROM Appointment a "
       + "LEFT JOIN a.doctor d "
       + "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) "
       + "AND a.patient.id = :patientId AND a.status = :status")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
       @Param("doctorName") String doctorName,
       @Param("patientId") Long patientId,
       @Param("status") AppointmentStatus status);

    private static AppointmentStatus statusFromOrdinal(int status) {
   AppointmentStatus[] statuses = AppointmentStatus.values();
   if (status < 0 || status >= statuses.length) {
       throw new IllegalArgumentException("Unknown appointment status: " + status);
   }
   return statuses[status];
    }

}

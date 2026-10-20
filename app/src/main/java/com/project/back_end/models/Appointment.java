package com.project.back_end.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;

@Entity
@Table(name = "appointment")
public class Appointment {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "doctor cannot be null")
  @ManyToOne
  @JoinColumn(name = "doctor_id", nullable = false)
  private Doctor doctor;

  @NotNull(message = "patient cannot be null")
  @ManyToOne
  @JoinColumn(name = "patient_id", nullable = false)
  private Patient patient;

  @NotNull(message = "appointment time cannot be null")
  @Future(message = "Appointment time must be in the future")
  private LocalDateTime appointmentTime;

  @NotNull(message = "status cannot be null")
  @Enumerated(EnumType.ORDINAL)
  private AppointmentStatus status;

  public enum AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED
  }

  public Appointment() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Doctor getDoctor() {
    return doctor;
  }

  public void setDoctor(Doctor doctor) {
    this.doctor = doctor;
  }

  public Patient getPatient() {
    return patient;
  }

  public void setPatient(Patient patient) {
    this.patient = patient;
  }

  public LocalDateTime getAppointmentTime() {
    return appointmentTime;
  }

  public void setAppointmentTime(LocalDateTime appointmentTime) {
    this.appointmentTime = appointmentTime;
  }

  public AppointmentStatus getStatus() {
    return status;
  }

  public void setStatus(AppointmentStatus status) {
    this.status = status;
  }

  @Transient
  public LocalDateTime getEndTime() {
    return appointmentTime == null ? null : appointmentTime.plusHours(1);
  }

  @Transient
  public LocalDate getAppointmentDate() {
    return appointmentTime == null ? null : appointmentTime.toLocalDate();
  }

  @Transient
  public LocalTime getAppointmentTimeOnly() {
    return appointmentTime == null ? null : appointmentTime.toLocalTime();
  }

}


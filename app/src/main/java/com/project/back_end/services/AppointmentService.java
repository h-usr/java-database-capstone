package com.project.back_end.services;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Appointment.AppointmentStatus;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@org.springframework.stereotype.Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        if (appointment == null
                || appointment.getDoctor() == null
                || appointment.getDoctor().getId() == null
                || appointment.getPatient() == null
                || appointment.getPatient().getId() == null
                || appointment.getAppointmentTime() == null) {
            return 0;
        }

        try {
            Optional<Doctor> doctor = doctorRepository.findById(appointment.getDoctor().getId());
            Optional<Patient> patient = patientRepository.findById(appointment.getPatient().getId());
            if (doctor.isEmpty() || patient.isEmpty()) {
                return 0;
            }

            LocalDateTime appointmentTime = appointment.getAppointmentTime();
            if (!isAvailable(doctor.get(), appointmentTime, null)) {
                return 0;
            }

            appointment.setDoctor(doctor.get());
            appointment.setPatient(patient.get());
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            appointmentRepository.save(appointment);
            return 1;
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getId() == null || appointment.getAppointmentTime() == null) {
            return response(HttpStatus.BAD_REQUEST, "Invalid appointment details");
        }

        try {
            Optional<Appointment> existingResult = appointmentRepository.findById(appointment.getId());
            if (existingResult.isEmpty()) {
                return response(HttpStatus.NOT_FOUND, "Appointment not found");
            }

            Appointment existing = existingResult.get();
            Long requestedPatientId = appointment.getPatient() == null
                    ? null
                    : appointment.getPatient().getId();
            if (requestedPatientId == null
                    || existing.getPatient() == null
                    || !Objects.equals(existing.getPatient().getId(), requestedPatientId)) {
                return response(HttpStatus.FORBIDDEN, "Appointment patient cannot be changed");
            }

            Long requestedDoctorId = appointment.getDoctor() == null
                    ? existing.getDoctor().getId()
                    : appointment.getDoctor().getId();
            if (requestedDoctorId == null) {
                return response(HttpStatus.BAD_REQUEST, "Doctor is required");
            }

            Optional<Doctor> doctor = doctorRepository.findById(requestedDoctorId);
            if (doctor.isEmpty()) {
                return response(HttpStatus.BAD_REQUEST, "Doctor not found");
            }

            if (!isAvailable(doctor.get(), appointment.getAppointmentTime(), existing.getId())) {
                return response(HttpStatus.CONFLICT, "Appointment time is unavailable");
            }

            existing.setDoctor(doctor.get());
            existing.setAppointmentTime(appointment.getAppointmentTime());
            appointmentRepository.save(existing);
            return response(HttpStatus.OK, "Appointment updated successfully");
        } catch (RuntimeException exception) {
            return response(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update appointment");
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        String email;
        try {
            email = tokenService.extractEmail(token);
        } catch (RuntimeException exception) {
            return response(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        try {
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                return response(HttpStatus.UNAUTHORIZED, "Patient not found");
            }

            Optional<Appointment> appointment = appointmentRepository.findById(id);
            if (appointment.isEmpty()) {
                return response(HttpStatus.NOT_FOUND, "Appointment not found");
            }

            Patient owner = appointment.get().getPatient();
            if (owner == null || !Objects.equals(owner.getId(), patient.getId())) {
                return response(HttpStatus.FORBIDDEN, "You cannot cancel this appointment");
            }

            appointmentRepository.delete(appointment.get());
            return response(HttpStatus.OK, "Appointment cancelled successfully");
        } catch (RuntimeException exception) {
            return response(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to cancel appointment");
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        if (date == null) {
            return Map.of("message", "Appointment date is required");
        }

        String email;
        try {
            email = tokenService.extractEmail(token);
        } catch (RuntimeException exception) {
            return Map.of("message", "Invalid or expired token");
        }

        try {
            Doctor doctor = doctorRepository.findByEmail(email);
            if (doctor == null) {
                return Map.of("message", "Doctor not found");
            }

            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);
            List<Appointment> appointments = pname == null || pname.isBlank()
                    ? appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                            doctor.getId(), start, end)
                    : appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                            doctor.getId(), pname.trim(), start, end);

            List<AppointmentDTO> appointmentDtos = new ArrayList<>();
            for (Appointment appointment : appointments) {
                appointmentDtos.add(toDto(appointment));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("appointments", appointmentDtos);
            return result;
        } catch (DateTimeException exception) {
            return Map.of("message", "Invalid appointment date");
        } catch (RuntimeException exception) {
            return Map.of("message", "Unable to retrieve appointments");
        }
    }

    private boolean isAvailable(Doctor doctor, LocalDateTime appointmentTime, Long excludedAppointmentId) {
        if (doctor.getAvailableTimes() == null
                || doctor.getAvailableTimes().stream().noneMatch(slot -> matchesTime(slot, appointmentTime.toLocalTime()))) {
            return false;
        }

        LocalDateTime start = appointmentTime.toLocalDate().atStartOfDay();
        LocalDateTime end = appointmentTime.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1);
        List<Appointment> sameDayAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctor.getId(), start, end);

        for (Appointment existing : sameDayAppointments) {
            if (Objects.equals(existing.getId(), excludedAppointmentId)
                    || existing.getStatus() == AppointmentStatus.CANCELLED
                    || existing.getAppointmentTime() == null) {
                continue;
            }

            LocalDateTime existingStart = existing.getAppointmentTime();
            LocalDateTime existingEnd = existingStart.plusHours(1);
            if (existingStart.isBefore(appointmentTime.plusHours(1))
                    && existingEnd.isAfter(appointmentTime)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesTime(String slot, LocalTime appointmentTime) {
        if (slot == null) {
            return false;
        }
        try {
            return LocalTime.parse(slot).equals(appointmentTime);
        } catch (DateTimeParseException exception) {
            return slot.equals(appointmentTime.toString());
        }
    }

    private AppointmentDTO toDto(Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        Patient patient = appointment.getPatient();
        return new AppointmentDTO(
                appointment.getId(),
                doctor.getId(),
                doctor.getName(),
                patient.getId(),
                patient.getName(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getAddress(),
                appointment.getAppointmentTime(),
                appointment.getStatus().ordinal());
    }

    private ResponseEntity<Map<String, String>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}

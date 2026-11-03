package com.project.back_end.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public int createPatient(Patient patient) {
        if (patient == null) {
            return 0;
        }
        try {
            patientRepository.save(patient);
            return 1;
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        if (id == null) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Patient ID is required");
        }

        try {
            String email = tokenService.extractIdentifier(token);
            Patient tokenPatient = patientRepository.findByEmail(email);
            if (tokenPatient == null || !Objects.equals(tokenPatient.getId(), id)) {
                return errorResponse(HttpStatus.UNAUTHORIZED, "Token does not belong to this patient");
            }

            List<AppointmentDTO> appointments = toDtos(appointmentRepository.findByPatientId(id));
            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointments);
            return ResponseEntity.ok(response);
        } catch (RuntimeException exception) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        if (id == null) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Patient ID is required");
        }

        int status;
        if (condition == null) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Condition must be past or future");
        }
        switch (condition.trim().toLowerCase()) {
            case "past" -> status = 1;
            case "future" -> status = 0;
            default -> {
                return errorResponse(HttpStatus.BAD_REQUEST, "Condition must be past or future");
            }
        }

        try {
            List<Appointment> appointments = appointmentRepository
                    .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status);
            return appointmentsResponse(appointments);
        } catch (RuntimeException exception) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to filter appointments");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        if (patientId == null || name == null || name.isBlank()) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Doctor name and patient ID are required");
        }

        try {
            return appointmentsResponse(
                    appointmentRepository.filterByDoctorNameAndPatientId(name.trim(), patientId));
        } catch (RuntimeException exception) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to filter appointments");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(
            String condition, String name, long patientId) {
        if (name == null || name.isBlank()) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Doctor name is required");
        }

        int status;
        if (condition == null) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Condition must be past or future");
        }
        switch (condition.trim().toLowerCase()) {
            case "past" -> status = 1;
            case "future" -> status = 0;
            default -> {
                return errorResponse(HttpStatus.BAD_REQUEST, "Condition must be past or future");
            }
        }

        try {
            List<Appointment> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientIdAndStatus(name.trim(), patientId, status);
            return appointmentsResponse(appointments);
        } catch (RuntimeException exception) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to filter appointments");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        try {
            String email = tokenService.extractIdentifier(token);
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                return errorResponse(HttpStatus.NOT_FOUND, "Patient not found");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("patient", patient);
            return ResponseEntity.ok(response);
        } catch (RuntimeException exception) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }

    private ResponseEntity<Map<String, Object>> appointmentsResponse(List<Appointment> appointments) {
        Map<String, Object> response = new HashMap<>();
        response.put("appointments", toDtos(appointments));
        return ResponseEntity.ok(response);
    }

    private List<AppointmentDTO> toDtos(List<Appointment> appointments) {
        List<AppointmentDTO> appointmentDtos = new ArrayList<>();
        for (Appointment appointment : appointments) {
            Doctor doctor = appointment.getDoctor();
            Patient patient = appointment.getPatient();
            appointmentDtos.add(new AppointmentDTO(
                    appointment.getId(),
                    doctor.getId(),
                    doctor.getName(),
                    patient.getId(),
                    patient.getName(),
                    patient.getEmail(),
                    patient.getPhone(),
                    patient.getAddress(),
                    appointment.getAppointmentTime(),
                    appointment.getStatus().ordinal()));
        }
        return appointmentDtos;
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}

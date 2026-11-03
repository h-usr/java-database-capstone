package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;

@RestController
@RequestMapping("/patient")
public class PatientController {
    private final PatientService patientService;
    private final Service service;

    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validationError(validation);
        }
        return patientService.getPatientDetails(token);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@Valid @RequestBody Patient patient) {
        try {
            if (!service.validatePatient(patient)) {
                return messageResponse(
                        HttpStatus.CONFLICT,
                        "Patient with email id or phone no already exist");
            }
            if (patientService.createPatient(patient) == 1) {
                return messageResponse(HttpStatus.CREATED, "Signup successful");
            }
            return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        } catch (RuntimeException exception) {
            return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    @GetMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(
            @PathVariable("id") Long id,
            @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validationError(validation);
        }
        return patientService.getPatientAppointment(id, token);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(
            @PathVariable("condition") String condition,
            @PathVariable("name") String name,
            @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validationError(validation);
        }

        String normalizedCondition = "upcoming".equalsIgnoreCase(condition)
                ? "future"
                : condition;
        return service.filterPatient(normalizedCondition, name, token);
    }

    private ResponseEntity<Map<String, Object>> validationError(
            ResponseEntity<Map<String, String>> validation) {
        Map<String, Object> body = new HashMap<>();
        if (validation.getBody() != null) {
            body.putAll(validation.getBody());
        }
        return ResponseEntity.status(validation.getStatusCode()).body(body);
    }

    private ResponseEntity<Map<String, String>> messageResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}



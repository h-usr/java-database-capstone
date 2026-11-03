package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable("date") LocalDate date,
            @PathVariable("patientName") String patientName,
            @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "doctor");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> body = new HashMap<>();
            if (validation.getBody() != null) {
                body.putAll(validation.getBody());
            }
            return ResponseEntity.status(validation.getStatusCode()).body(body);
        }

        return ResponseEntity.ok(appointmentService.getAppointment(patientName, date, token));
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @PathVariable("token") String token,
            @Valid @RequestBody Appointment appointment) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validation;
        }

        int appointmentStatus = service.validateAppointment(appointment);
        if (appointmentStatus == -1) {
            return messageResponse(HttpStatus.NOT_FOUND, "Doctor not found");
        }
        if (appointmentStatus == 0) {
            return messageResponse(HttpStatus.CONFLICT, "Appointment time is unavailable");
        }
        if (appointmentService.bookAppointment(appointment) == 1) {
            return messageResponse(HttpStatus.CREATED, "Appointment booked successfully");
        }
        return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to book appointment");
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @PathVariable("token") String token,
            @Valid @RequestBody Appointment appointment) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validation;
        }
        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable("id") long id,
            @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validation;
        }
        return appointmentService.cancelAppointment(id, token);
    }

    private ResponseEntity<Map<String, String>> messageResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}

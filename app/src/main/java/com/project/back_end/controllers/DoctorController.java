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

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;

@RestController
@RequestMapping("${api.path}" + "doctor")
public class DoctorController {
    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable("user") String user,
            @PathVariable("doctorId") Long doctorId,
            @PathVariable("date") LocalDate date,
            @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, user);
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validationError(validation);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("availability", doctorService.getDoctorAvailability(doctorId, date));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
        return ResponseEntity.ok(Map.of("doctors", doctorService.getDoctors()));
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(
            @PathVariable("token") String token,
            @Valid @RequestBody Doctor doctor) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "admin");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validation;
        }

        int result = doctorService.saveDoctor(doctor);
        if (result == 1) {
            return messageResponse(HttpStatus.CREATED, "Doctor added to db");
        }
        if (result == -1) {
            return messageResponse(HttpStatus.CONFLICT, "Doctor already exists");
        }
        return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Some internal error occurred");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@Valid @RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @PathVariable("token") String token,
            @Valid @RequestBody Doctor doctor) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "admin");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validation;
        }

        int result = doctorService.updateDoctor(doctor);
        if (result == 1) {
            return messageResponse(HttpStatus.OK, "Doctor updated");
        }
        if (result == -1) {
            return messageResponse(HttpStatus.NOT_FOUND, "Doctor not found");
        }
        return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Some internal error occurred");
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable("id") long id,
            @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "admin");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validation;
        }

        int result = doctorService.deleteDoctor(id);
        if (result == 1) {
            return messageResponse(HttpStatus.OK, "Doctor deleted successfully");
        }
        if (result == -1) {
            return messageResponse(HttpStatus.NOT_FOUND, "Doctor not found with id " + id);
        }
        return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Some internal error occurred");
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(
            @PathVariable("name") String name,
            @PathVariable("time") String time,
            @PathVariable("speciality") String speciality) {
        return ResponseEntity.ok(service.filterDoctor(name, speciality, time));
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

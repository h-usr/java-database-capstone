package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.List;
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

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;

@RestController
@RequestMapping("${api.path}" + "prescription")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final Service service;

    public PrescriptionController(PrescriptionService prescriptionService, Service service) {
        this.prescriptionService = prescriptionService;
        this.service = service;
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @PathVariable("token") String token,
            @Valid @RequestBody Prescription prescription) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "doctor");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validation;
        }
        return prescriptionService.savePrescription(prescription);
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable("appointmentId") Long appointmentId,
            @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "doctor");
        if (!validation.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> body = new HashMap<>();
            if (validation.getBody() != null) {
                body.putAll(validation.getBody());
            }
            return ResponseEntity.status(validation.getStatusCode()).body(body);
        }

        ResponseEntity<Map<String, Object>> response = prescriptionService.getPrescription(appointmentId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        Object prescriptions = response.getBody() == null
                ? null
                : response.getBody().get("prescriptions");
        if (prescriptions instanceof List<?> prescriptionList && prescriptionList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No prescription exists for this appointment"));
        }
        return response;
    }
}

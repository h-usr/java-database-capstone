package com.project.back_end.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;

@Service
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        if (prescription == null) {
            return messageResponse(HttpStatus.BAD_REQUEST, "Prescription is required");
        }

        try {
            prescriptionRepository.save(prescription);
            return messageResponse(HttpStatus.CREATED, "Prescription saved");
        } catch (RuntimeException exception) {
            return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to save prescription");
        }
    }

    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        if (appointmentId == null) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Appointment ID is required");
        }

        try {
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
            Map<String, Object> response = new HashMap<>();
            response.put("prescriptions", prescriptions);
            return ResponseEntity.ok(response);
        } catch (RuntimeException exception) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve prescription");
        }
    }

    private ResponseEntity<Map<String, String>> messageResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}

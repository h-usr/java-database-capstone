package com.project.back_end.services;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@org.springframework.stereotype.Service
public class Service {
    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(
            TokenService tokenService,
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DoctorService doctorService,
            PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        try {
            if (tokenService.validateToken(token, user)) {
                return ResponseEntity.ok(Map.of());
            }
        } catch (RuntimeException exception) {
            return tokenError("Invalid or expired token");
        }
        return tokenError("Invalid or expired token");
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        if (receivedAdmin == null
                || receivedAdmin.getUsername() == null
                || receivedAdmin.getPassword() == null) {
            return messageResponse(HttpStatus.BAD_REQUEST, "Username and password are required");
        }

        try {
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
            if (admin == null || !Objects.equals(admin.getPassword(), receivedAdmin.getPassword())) {
                return messageResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            }
            return ResponseEntity.ok(Map.of("token", tokenService.generateToken(admin.getUsername())));
        } catch (RuntimeException exception) {
            return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to validate admin credentials");
        }
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        boolean hasName = hasText(name);
        boolean hasSpecialty = hasText(specialty);
        boolean hasTime = hasText(time);

        if (hasName && hasSpecialty && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name.trim(), specialty.trim(), normalizePeriod(time));
        }
        if (hasName && hasSpecialty) {
            return doctorService.filterDoctorByNameAndSpecility(name.trim(), specialty.trim());
        }
        if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(name.trim(), normalizePeriod(time));
        }
        if (hasSpecialty && hasTime) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty.trim(), normalizePeriod(time));
        }
        if (hasName) {
            return doctorService.findDoctorByName(name.trim());
        }
        if (hasSpecialty) {
            return doctorService.filterDoctorBySpecility(specialty.trim());
        }
        if (hasTime) {
            return doctorService.filterDoctorsByTime(normalizePeriod(time));
        }
        return Map.of("doctors", doctorService.getDoctors());
    }

    public int validateAppointment(Appointment appointment) {
        if (appointment == null
                || appointment.getDoctor() == null
                || appointment.getDoctor().getId() == null
                || appointment.getAppointmentTime() == null) {
            return 0;
        }

        try {
            Long doctorId = appointment.getDoctor().getId();
            if (doctorRepository.findById(doctorId).isEmpty()) {
                return -1;
            }

            LocalTime requestedTime = appointment.getAppointmentTime().toLocalTime();
            List<String> availableTimes = doctorService.getDoctorAvailability(
                    doctorId, appointment.getAppointmentTime().toLocalDate());
            return availableTimes.stream().anyMatch(slot -> matchesSlotStart(slot, requestedTime)) ? 1 : 0;
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    public boolean validatePatient(Patient patient) {
        if (patient == null || (!hasText(patient.getEmail()) && !hasText(patient.getPhone()))) {
            return false;
        }
        return patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone()) == null;
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        if (login == null || !hasText(login.getIdentifier()) || login.getPassword() == null) {
            return messageResponse(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        try {
            Patient patient = patientRepository.findByEmail(login.getIdentifier());
            if (patient == null || !Objects.equals(patient.getPassword(), login.getPassword())) {
                return messageResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password");
            }
            return ResponseEntity.ok(Map.of("token", tokenService.generateToken(patient.getEmail())));
        } catch (RuntimeException exception) {
            return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to validate patient credentials");
        }
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        if (!tokenService.validateToken(token, "patient")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token"));
        }

        try {
            String email = tokenService.extractIdentifier(token);
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Patient account not found"));
            }

            boolean hasCondition = hasText(condition);
            boolean hasName = hasText(name);
            if (hasCondition && hasName) {
                return patientService.filterByDoctorAndCondition(
                        condition.trim(), name.trim(), patient.getId());
            }
            if (hasCondition) {
                return patientService.filterByCondition(condition.trim(), patient.getId());
            }
            if (hasName) {
                return patientService.filterByDoctor(name.trim(), patient.getId());
            }
            return patientService.getPatientAppointment(patient.getId(), token);
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to filter patient appointments"));
        }
    }

    private boolean matchesSlotStart(String slot, LocalTime requestedTime) {
        if (!hasText(slot)) {
            return false;
        }
        String start = slot.split("-", 2)[0].trim();
        try {
            return LocalTime.parse(start).equals(requestedTime);
        } catch (DateTimeParseException exception) {
            return start.equals(requestedTime.toString());
        }
    }

    private String normalizePeriod(String time) {
        return time == null ? null : time.trim().toUpperCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private ResponseEntity<Map<String, String>> tokenError(String message) {
        return messageResponse(HttpStatus.UNAUTHORIZED, message);
    }

    private ResponseEntity<Map<String, String>> messageResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}

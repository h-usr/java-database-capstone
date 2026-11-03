package com.project.back_end.services;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Appointment.AppointmentStatus;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@org.springframework.stereotype.Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    @Autowired(required = false)
    private TokenService tokenService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        if (doctorId == null || date == null) {
            return List.of();
        }

        Optional<Doctor> doctorResult = doctorRepository.findById(doctorId);
        if (doctorResult.isEmpty() || doctorResult.get().getAvailableTimes() == null) {
            return List.of();
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
        List<LocalTime> bookedStarts = appointments.stream()
                .filter(appointment -> appointment.getStatus() != AppointmentStatus.CANCELLED)
                .map(Appointment::getAppointmentTime)
                .filter(appointmentTime -> appointmentTime != null)
                .map(LocalDateTime::toLocalTime)
                .toList();

        return doctorResult.get().getAvailableTimes().stream()
                .filter(slot -> {
                    LocalTime slotStart = getSlotStart(slot);
                    return slotStart != null && !bookedStarts.contains(slotStart);
                })
                .toList();
    }

    @Transactional
    public int saveDoctor(Doctor doctor) {
        if (doctor == null || doctor.getEmail() == null || doctor.getEmail().isBlank()) {
            return 0;
        }
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    @Transactional
    public int updateDoctor(Doctor doctor) {
        if (doctor == null || doctor.getId() == null) {
            return -1;
        }
        try {
            if (!doctorRepository.existsById(doctor.getId())) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        initializeAvailableTimes(doctors);
        return doctors;
    }

    @Transactional
    public int deleteDoctor(long id) {
        try {
            Optional<Doctor> doctor = doctorRepository.findById(id);
            if (doctor.isEmpty()) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.delete(doctor.get());
            return 1;
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        if (login == null || login.getIdentifier() == null || login.getPassword() == null) {
            return messageResponse(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        try {
            Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());
            if (doctor == null || !doctor.getPassword().equals(login.getPassword())) {
                return messageResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password");
            }

            SecretKey signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date issuedAt = new Date();
            String token = Jwts.builder()
                    .subject(doctor.getEmail())
                    .issuedAt(issuedAt)
                    .expiration(new Date(issuedAt.getTime() + 7L * 24 * 60 * 60 * 1000))
                    .signWith(signingKey)
                    .compact();

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException exception) {
            return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to validate doctor credentials");
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> findDoctorByName(String name) {
        List<Doctor> doctors = name == null || name.isBlank()
                ? doctorRepository.findAll()
                : doctorRepository.findByNameLike(name.trim());
        return doctorsResult(doctors);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(
            String name, String specialty, String amOrPm) {
        List<Doctor> doctors = doctorRepository
                .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return doctorsResult(filterDoctorByTime(doctors, amOrPm));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        List<Doctor> doctors = name == null || name.isBlank()
                ? doctorRepository.findAll()
                : doctorRepository.findByNameLike(name.trim());
        return doctorsResult(filterDoctorByTime(doctors, amOrPm));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specilty) {
        return doctorsResult(doctorRepository
                .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specilty));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specilty, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        return doctorsResult(filterDoctorByTime(doctors, amOrPm));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorBySpecility(String specilty) {
        return doctorsResult(doctorRepository.findBySpecialtyIgnoreCase(specilty));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        return doctorsResult(filterDoctorByTime(doctorRepository.findAll(), amOrPm));
    }

    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (doctors == null || amOrPm == null) {
            return List.of();
        }
        String period = amOrPm.trim().toUpperCase(Locale.ROOT);
        if (!period.equals("AM") && !period.equals("PM")) {
            return List.of();
        }

        List<Doctor> matchingDoctors = new ArrayList<>();
        for (Doctor doctor : doctors) {
            List<String> availableTimes = doctor.getAvailableTimes();
            if (availableTimes != null && availableTimes.stream().anyMatch(slot -> {
                LocalTime slotStart = getSlotStart(slot);
                return slotStart != null && (period.equals("AM")
                        ? slotStart.isBefore(LocalTime.NOON)
                        : !slotStart.isBefore(LocalTime.NOON));
            })) {
                matchingDoctors.add(doctor);
            }
        }
        return matchingDoctors;
    }

    private LocalTime getSlotStart(String slot) {
        if (slot == null || slot.isBlank()) {
            return null;
        }
        String start = slot.split("-", 2)[0].trim();
        try {
            return LocalTime.parse(start);
        } catch (DateTimeParseException exception) {
            try {
                return LocalTime.parse(start, DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private Map<String, Object> doctorsResult(List<Doctor> doctors) {
        initializeAvailableTimes(doctors);
        Map<String, Object> result = new HashMap<>();
        result.put("doctors", doctors);
        return result;
    }

    private void initializeAvailableTimes(List<Doctor> doctors) {
        for (Doctor doctor : doctors) {
            if (doctor.getAvailableTimes() != null) {
                doctor.getAvailableTimes().size();
            }
        }
    }

    private ResponseEntity<Map<String, String>> messageResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}

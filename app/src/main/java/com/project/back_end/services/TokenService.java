package com.project.back_end.services;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenService {
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final String secret;

    public TokenService(
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            @Value("${jwt.secret}") String secret) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.secret = secret;
    }

    public String generateToken(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Identifier is required");
        }

        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .subject(identifier)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(7, ChronoUnit.DAYS)))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractIdentifier(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }

        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .getSubject();
    }

    public String extractEmail(String token) {
        return extractIdentifier(token);
    }

    public boolean validateToken(String token, String user) {
        if (user == null || user.isBlank()) {
            return false;
        }

        try {
            String identifier = extractIdentifier(token);
            return switch (user.toLowerCase(Locale.ROOT)) {
                case "admin" -> adminRepository.findByUsername(identifier) != null;
                case "doctor" -> doctorRepository.findByEmail(identifier) != null;
                case "patient" -> patientRepository.findByEmail(identifier) != null;
                default -> false;
            };
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

package com.miniprontuario.miniprontuario_backend.service;

import com.miniprontuario.miniprontuario_backend.dto.AuthDTOs.*;
import com.miniprontuario.miniprontuario_backend.exception.BusinessException;
import com.miniprontuario.miniprontuario_backend.exception.DuplicateResourceException;
import com.miniprontuario.miniprontuario_backend.model.Dentist;
import com.miniprontuario.miniprontuario_backend.model.RefreshToken;
import com.miniprontuario.miniprontuario_backend.repository.DentistRepository;
import com.miniprontuario.miniprontuario_backend.repository.RefreshTokenRepository;
import com.miniprontuario.miniprontuario_backend.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    /** Refresh tokens are valid for 7 days (as per spec requirement). */
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    private static final String CRO_PATTERN = "^[A-Z]{2}-?\\d{4,6}$";

    private final DentistRepository dentistRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void register(RegisterRequest request) {
        if (dentistRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (dentistRepository.existsByCpf(request.getCpf())) {
            throw new DuplicateResourceException("CPF already registered");
        }
        if (dentistRepository.existsByCro(request.getCro())) {
            throw new DuplicateResourceException("CRO already registered");
        }

        // Validate CRO format (e.g., SP-12345 or SP123456)
        System.out.println("DEBUG: Received CRO for validation: [" + request.getCro() + "]");
        if (!request.getCro().toUpperCase().matches(CRO_PATTERN)) {
            throw new BusinessException("CRO format is invalid. Expected format: <UF>-<digits> (e.g., SP-12345)");
        }

        Dentist dentist = Dentist.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .cpf(request.getCpf())
                .cro(request.getCro().toUpperCase())
                .phone(request.getPhone())
                .build();

        dentistRepository.save(dentist);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Dentist dentist = dentistRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), dentist.getPassword())) {
            throw new BusinessException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateToken(dentist.getId(), dentist.getEmail());
        RefreshToken refreshToken = createRefreshToken(dentist);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtUtil.getExpiration() / 1000)
                .user(UserResponse.builder()
                        .id(dentist.getId().toString())
                        .name(dentist.getName())
                        .email(dentist.getEmail())
                        .build())
                .build();
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (stored.isRevoked()) {
            throw new BusinessException("Refresh token has been revoked");
        }
        if (stored.isExpired()) {
            throw new BusinessException("Refresh token has expired");
        }

        // Revoke the used token (rotation: one-time use)
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        Dentist dentist = stored.getDentist();
        String newAccessToken = jwtUtil.generateToken(dentist.getId(), dentist.getEmail());
        RefreshToken newRefreshToken = createRefreshToken(dentist);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .expiresIn(jwtUtil.getExpiration() / 1000)
                .user(UserResponse.builder()
                        .id(dentist.getId().toString())
                        .name(dentist.getName())
                        .email(dentist.getEmail())
                        .build())
                .build();
    }

    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
    }

    public MeResponse getMe(UUID dentistId) {
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new BusinessException("User not found"));
        return MeResponse.builder()
                .id(dentist.getId().toString())
                .name(dentist.getName())
                .email(dentist.getEmail())
                .cpf(dentist.getCpf())
                .cro(dentist.getCro())
                .phone(dentist.getPhone())
                .build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private RefreshToken createRefreshToken(Dentist dentist) {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .dentist(dentist)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }
}

package com.miniprontuario.miniprontuario_backend.service;

import com.miniprontuario.miniprontuario_backend.dto.AuthDTOs.*;
import com.miniprontuario.miniprontuario_backend.exception.BusinessException;
import com.miniprontuario.miniprontuario_backend.exception.DuplicateResourceException;
import com.miniprontuario.miniprontuario_backend.model.Dentist;
import com.miniprontuario.miniprontuario_backend.repository.DentistRepository;
import com.miniprontuario.miniprontuario_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final DentistRepository dentistRepository;
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

        Dentist dentist = Dentist.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .cpf(request.getCpf())
                .cro(request.getCro())
                .phone(request.getPhone())
                .build();

        dentistRepository.save(dentist);
    }

    public AuthResponse login(LoginRequest request) {
        Dentist dentist = dentistRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), dentist.getPassword())) {
            throw new BusinessException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(dentist.getId(), dentist.getEmail());
        return new AuthResponse(token);
    }
}

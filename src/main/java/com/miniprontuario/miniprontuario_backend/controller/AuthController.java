package com.miniprontuario.miniprontuario_backend.controller;

import com.miniprontuario.miniprontuario_backend.dto.AuthDTOs.*;
import com.miniprontuario.miniprontuario_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for dentist registration and login")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @org.springframework.web.bind.annotation.GetMapping("/me")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get current authenticated dentist profile")
    public ResponseEntity<MeResponse> getMe(@org.springframework.security.core.annotation.AuthenticationPrincipal com.miniprontuario.miniprontuario_backend.security.DentistPrincipal principal) {
        MeResponse response = authService.getMe(principal.getId());
        return ResponseEntity.ok(response);
    }
}

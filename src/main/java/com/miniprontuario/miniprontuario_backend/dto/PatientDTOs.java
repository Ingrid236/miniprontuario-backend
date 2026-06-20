package com.miniprontuario.miniprontuario_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PatientDTOs {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "CPF is required")
        @Size(min = 11, max = 14, message = "CPF must be between 11 and 14 characters")
        private String cpf;

        @NotNull(message = "Birth date is required")
        @PastOrPresent(message = "Birth date cannot be in the future")
        private LocalDate birthDate;

        private String phone;

        private String allergies;

        private String systemicDiseases;

        private String medications;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientResponse {
        private UUID id;
        private String name;
        private String cpf;
        private LocalDate birthDate;
        private String phone;
        private String allergies;
        private String systemicDiseases;
        private String medications;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}

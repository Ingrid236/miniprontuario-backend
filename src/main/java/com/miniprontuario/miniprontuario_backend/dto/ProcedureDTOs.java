package com.miniprontuario.miniprontuario_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ProcedureDTOs {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcedureRequest {
        @NotNull(message = "Procedure date is required")
        private LocalDate date;

        @NotBlank(message = "Procedure description is required")
        private String description;

        private String tooth;
        private String notes;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcedureResponse {
        private UUID id;
        private UUID patientId;
        private LocalDate date;
        private String description;
        private String tooth;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}

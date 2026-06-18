package com.miniprontuario.miniprontuario_backend.controller;

import com.miniprontuario.miniprontuario_backend.dto.AiAnalysisResponse;
import com.miniprontuario.miniprontuario_backend.security.DentistPrincipal;
import com.miniprontuario.miniprontuario_backend.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Clinical Assistant", description = "Endpoints for clinical AI analytics and assistant features")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiService aiService;

    @PostMapping("/analyze-patient/{patientId}")
    @Operation(summary = "Generate a clinical risk analysis for a patient based on their medical record and history")
    public ResponseEntity<AiAnalysisResponse> analyzePatient(
            @PathVariable UUID patientId,
            @AuthenticationPrincipal DentistPrincipal principal) {
        AiAnalysisResponse response = aiService.analyzePatient(patientId, principal.getId());
        return ResponseEntity.ok(response);
    }
}

package com.miniprontuario.miniprontuario_backend.controller;

import com.miniprontuario.miniprontuario_backend.dto.ProcedureDTOs.*;
import com.miniprontuario.miniprontuario_backend.service.ProcedureService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@Tag(name = "Procedures", description = "Endpoints for tracking dental procedures")
public class ProcedureController {

    private final ProcedureService procedureService;

    @PostMapping("/patients/{patientId}/procedures")
    public ResponseEntity<ProcedureResponse> createProcedure(
            @PathVariable UUID patientId,
            @Valid @RequestBody ProcedureRequest request) {
        ProcedureResponse response = procedureService.createProcedure(patientId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/patients/{patientId}/procedures")
    public ResponseEntity<List<ProcedureResponse>> listProcedures(@PathVariable UUID patientId) {
        List<ProcedureResponse> response = procedureService.listProcedures(patientId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/procedures/{id}")
    public ResponseEntity<ProcedureResponse> updateProcedure(
            @PathVariable UUID id,
            @Valid @RequestBody ProcedureRequest request) {
        ProcedureResponse response = procedureService.updateProcedure(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/procedures/{id}")
    public ResponseEntity<Void> deleteProcedure(@PathVariable UUID id) {
        procedureService.deleteProcedure(id);
        return ResponseEntity.noContent().build();
    }
}

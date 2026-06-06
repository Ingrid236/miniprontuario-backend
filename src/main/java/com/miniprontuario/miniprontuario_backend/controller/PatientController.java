package com.miniprontuario.miniprontuario_backend.controller;

import com.miniprontuario.miniprontuario_backend.dto.PatientDTOs.*;
import com.miniprontuario.miniprontuario_backend.service.PatientService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Endpoints for dentist's patient management")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponse> registerPatient(@Valid @RequestBody PatientRequest request) {
        PatientResponse response = patientService.registerPatient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> listPatients() {
        List<PatientResponse> response = patientService.listPatients();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable UUID id) {
        PatientResponse response = patientService.getPatient(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}

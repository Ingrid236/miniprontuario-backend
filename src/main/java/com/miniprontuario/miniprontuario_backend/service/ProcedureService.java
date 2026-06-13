package com.miniprontuario.miniprontuario_backend.service;

import com.miniprontuario.miniprontuario_backend.dto.ProcedureDTOs.*;
import com.miniprontuario.miniprontuario_backend.exception.BusinessException;
import com.miniprontuario.miniprontuario_backend.exception.ResourceNotFoundException;
import com.miniprontuario.miniprontuario_backend.model.Patient;
import com.miniprontuario.miniprontuario_backend.model.Procedure;
import com.miniprontuario.miniprontuario_backend.repository.PatientRepository;
import com.miniprontuario.miniprontuario_backend.repository.ProcedureRepository;
import com.miniprontuario.miniprontuario_backend.security.DentistPrincipal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcedureService {

    private final ProcedureRepository procedureRepository;
    private final PatientRepository patientRepository;

    private DentistPrincipal getAuthenticatedDentist() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof DentistPrincipal) {
            return (DentistPrincipal) principal;
        }
        throw new BusinessException("User not authenticated");
    }

    @Transactional
    public ProcedureResponse createProcedure(UUID patientId, ProcedureRequest request) {
        DentistPrincipal dentistPrincipal = getAuthenticatedDentist();

        Patient patient = patientRepository.findByIdAndDentistId(patientId, dentistPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found or unauthorized"));

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Procedure date cannot be in the future");
        }

        // Validate FDI tooth notation if provided
        if (request.getTooth() != null && !request.getTooth().isBlank()) {
            if (!request.getTooth().matches("^[1-8][1-8]$")) {
                throw new BusinessException("Tooth must use FDI notation (e.g., 11, 48). Format: first digit quadrant (1-8), second digit position (1-8)");
            }
        }

        Procedure procedure = Procedure.builder()
                .patient(patient)
                .date(request.getDate())
                .description(request.getDescription())
                .tooth(request.getTooth())
                .notes(request.getNotes())
                .status(request.getStatus() != null ? request.getStatus() : "PLANNED")
                .cost(request.getCost())
                .deleted(false)
                .build();

        Procedure saved = procedureRepository.save(procedure);
        return mapToResponse(saved);
    }

    public List<ProcedureResponse> listProcedures(UUID patientId) {
        DentistPrincipal dentistPrincipal = getAuthenticatedDentist();

        // Enforce patient ownership
        patientRepository.findByIdAndDentistId(patientId, dentistPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found or unauthorized"));

        return procedureRepository.findByPatientIdAndPatientDentistId(patientId, dentistPrincipal.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProcedureResponse updateProcedure(UUID id, ProcedureRequest request) {
        DentistPrincipal dentistPrincipal = getAuthenticatedDentist();

        Procedure procedure = procedureRepository.findByIdAndPatientDentistId(id, dentistPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Procedure not found or unauthorized"));

        // Enforce 24-hour edit window
        if (procedure.getCreatedAt().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new BusinessException("Procedures can only be edited within 24 hours of creation");
        }

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Procedure date cannot be in the future");
        }

        // Validate FDI tooth notation if provided
        if (request.getTooth() != null && !request.getTooth().isBlank()) {
            if (!request.getTooth().matches("^[1-8][1-8]$")) {
                throw new BusinessException("Tooth must use FDI notation (e.g., 11, 48). Format: first digit quadrant (1-8), second digit position (1-8)");
            }
        }

        procedure.setDate(request.getDate());
        procedure.setDescription(request.getDescription());
        procedure.setTooth(request.getTooth());
        procedure.setNotes(request.getNotes());
        if (request.getStatus() != null) {
            procedure.setStatus(request.getStatus());
        }
        procedure.setCost(request.getCost());

        Procedure updated = procedureRepository.save(procedure);
        return mapToResponse(updated);
    }

    public ProcedureResponse getProcedure(UUID id) {
        DentistPrincipal dentistPrincipal = getAuthenticatedDentist();
        Procedure procedure = procedureRepository.findByIdAndPatientDentistId(id, dentistPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Procedure not found or unauthorized"));
        return mapToResponse(procedure);
    }

    @Transactional
    public void deleteProcedure(UUID id) {
        DentistPrincipal dentistPrincipal = getAuthenticatedDentist();

        Procedure procedure = procedureRepository.findByIdAndPatientDentistId(id, dentistPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Procedure not found or unauthorized"));

        procedure.setDeleted(true);
        procedureRepository.save(procedure);
    }

    private ProcedureResponse mapToResponse(Procedure procedure) {
        return ProcedureResponse.builder()
                .id(procedure.getId())
                .patientId(procedure.getPatient().getId())
                .date(procedure.getDate())
                .description(procedure.getDescription())
                .tooth(procedure.getTooth())
                .notes(procedure.getNotes())
                .status(procedure.getStatus())
                .cost(procedure.getCost())
                .createdAt(procedure.getCreatedAt())
                .updatedAt(procedure.getUpdatedAt())
                .build();
    }
}

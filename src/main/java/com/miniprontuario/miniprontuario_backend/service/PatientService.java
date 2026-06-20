package com.miniprontuario.miniprontuario_backend.service;

import com.miniprontuario.miniprontuario_backend.dto.PatientDTOs.*;
import com.miniprontuario.miniprontuario_backend.exception.BusinessException;
import com.miniprontuario.miniprontuario_backend.exception.DuplicateResourceException;
import com.miniprontuario.miniprontuario_backend.exception.ResourceNotFoundException;
import com.miniprontuario.miniprontuario_backend.model.Dentist;
import com.miniprontuario.miniprontuario_backend.model.Patient;
import com.miniprontuario.miniprontuario_backend.repository.DentistRepository;
import com.miniprontuario.miniprontuario_backend.repository.PatientRepository;
import com.miniprontuario.miniprontuario_backend.security.DentistPrincipal;
import com.miniprontuario.miniprontuario_backend.util.CpfValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final DentistRepository dentistRepository;

    private DentistPrincipal getAuthenticatedDentist() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof DentistPrincipal) {
            return (DentistPrincipal) principal;
        }
        throw new BusinessException("User not authenticated");
    }

    @Transactional
    public PatientResponse registerPatient(PatientRequest request) {
        DentistPrincipal dentistPrincipal = getAuthenticatedDentist();

        // Validate CPF digit algorithm
        if (!CpfValidator.isValid(request.getCpf())) {
            throw new BusinessException("Invalid CPF: failed digit verification");
        }

        // Validate birth date: must be in the past and patient age <= 120
        if (request.getBirthDate() != null) {
            if (request.getBirthDate().isAfter(LocalDate.now())) {
                throw new BusinessException("Birth date cannot be in the future");
            }
            long age = java.time.Period.between(request.getBirthDate(), LocalDate.now()).getYears();
            if (age > 120) {
                throw new BusinessException("Birth date implies an age greater than 120 years");
            }
        }

        if (patientRepository.existsByDentistIdAndCpf(dentistPrincipal.getId(), request.getCpf())) {
            throw new DuplicateResourceException("Patient with this CPF already registered for this dentist");
        }

        Dentist dentist = dentistRepository.findById(dentistPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found"));

        Patient patient = Patient.builder()
                .dentist(dentist)
                .name(request.getName())
                .cpf(request.getCpf())
                .birthDate(request.getBirthDate())
                .phone(request.getPhone())
                .allergies(request.getAllergies())
                .systemicDiseases(request.getSystemicDiseases())
                .medications(request.getMedications())
                .deleted(false)
                .build();

        Patient saved = patientRepository.save(patient);
        return mapToResponse(saved);
    }

    public List<PatientResponse> listPatients() {
        DentistPrincipal dentistPrincipal = getAuthenticatedDentist();
        return patientRepository.findByDentistId(dentistPrincipal.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PatientResponse getPatient(UUID id) {
        DentistPrincipal dentistPrincipal = getAuthenticatedDentist();
        Patient patient = patientRepository.findByIdAndDentistId(id, dentistPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found or unauthorized"));
        return mapToResponse(patient);
    }

    @Transactional
    public void deletePatient(UUID id) {
        DentistPrincipal dentistPrincipal = getAuthenticatedDentist();
        Patient patient = patientRepository.findByIdAndDentistId(id, dentistPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found or unauthorized"));
        patient.setDeleted(true);
        patientRepository.save(patient);
    }

    @Transactional
    public PatientResponse updatePatient(UUID id, PatientRequest request) {
        DentistPrincipal dentistPrincipal = getAuthenticatedDentist();
        Patient patient = patientRepository.findByIdAndDentistId(id, dentistPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found or unauthorized"));

        // Validate CPF if it has changed
        if (!patient.getCpf().equals(request.getCpf())) {
            if (!CpfValidator.isValid(request.getCpf())) {
                throw new BusinessException("Invalid CPF: failed digit verification");
            }
            if (patientRepository.existsByDentistIdAndCpf(dentistPrincipal.getId(), request.getCpf())) {
                throw new DuplicateResourceException("Patient with this CPF already registered for this dentist");
            }
            patient.setCpf(request.getCpf());
        }

        // Validate birth date: must be in the past and patient age <= 120
        if (request.getBirthDate() != null) {
            if (request.getBirthDate().isAfter(LocalDate.now())) {
                throw new BusinessException("Birth date cannot be in the future");
            }
            long age = java.time.Period.between(request.getBirthDate(), LocalDate.now()).getYears();
            if (age > 120) {
                throw new BusinessException("Birth date implies an age greater than 120 years");
            }
            patient.setBirthDate(request.getBirthDate());
        }

        patient.setName(request.getName());
        patient.setPhone(request.getPhone());
        patient.setAllergies(request.getAllergies());
        patient.setSystemicDiseases(request.getSystemicDiseases());
        patient.setMedications(request.getMedications());

        Patient saved = patientRepository.save(patient);
        return mapToResponse(saved);
    }

    private PatientResponse mapToResponse(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .name(patient.getName())
                .cpf(patient.getCpf())
                .birthDate(patient.getBirthDate())
                .phone(patient.getPhone())
                .allergies(patient.getAllergies())
                .systemicDiseases(patient.getSystemicDiseases())
                .medications(patient.getMedications())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}

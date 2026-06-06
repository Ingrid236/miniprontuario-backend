package com.miniprontuario.miniprontuario_backend.unit;

import com.miniprontuario.miniprontuario_backend.dto.PatientDTOs.*;
import com.miniprontuario.miniprontuario_backend.exception.DuplicateResourceException;
import com.miniprontuario.miniprontuario_backend.model.Dentist;
import com.miniprontuario.miniprontuario_backend.model.Patient;
import com.miniprontuario.miniprontuario_backend.repository.DentistRepository;
import com.miniprontuario.miniprontuario_backend.repository.PatientRepository;
import com.miniprontuario.miniprontuario_backend.security.DentistPrincipal;
import com.miniprontuario.miniprontuario_backend.service.PatientService;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DentistRepository dentistRepository;

    @InjectMocks
    private PatientService patientService;

    private DentistPrincipal principal;
    private Dentist dentist;

    @BeforeEach
    void setUp() {
        UUID dentistId = UUID.randomUUID();
        principal = new DentistPrincipal(dentistId, "dentist@example.com");
        dentist = Dentist.builder().name("Dr. Bob").email("dentist@example.com").build();
        dentist.setId(dentistId);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, null
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void registerPatient_ShouldCreatePatient_WhenValidRequest() {
        PatientRequest request = PatientRequest.builder()
                .name("John Doe")
                .cpf("98765432100")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        when(patientRepository.existsByDentistIdAndCpf(principal.getId(), "98765432100")).thenReturn(false);
        when(dentistRepository.findById(principal.getId())).thenReturn(Optional.of(dentist));

        Patient patient = Patient.builder()
                .dentist(dentist)
                .name("John Doe")
                .cpf("98765432100")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        patient.setId(UUID.randomUUID());

        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientResponse response = patientService.registerPatient(request);

        assertNotNull(response);
        assertEquals("John Doe", response.getName());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void registerPatient_ShouldThrowException_WhenCpfDuplicate() {
        PatientRequest request = PatientRequest.builder()
                .name("John Doe")
                .cpf("98765432100")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        when(patientRepository.existsByDentistIdAndCpf(principal.getId(), "98765432100")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> patientService.registerPatient(request));
        verify(patientRepository, never()).save(any());
    }
}

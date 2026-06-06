package com.miniprontuario.miniprontuario_backend.unit;

import com.miniprontuario.miniprontuario_backend.dto.ProcedureDTOs.*;
import com.miniprontuario.miniprontuario_backend.exception.BusinessException;
import com.miniprontuario.miniprontuario_backend.exception.ResourceNotFoundException;
import com.miniprontuario.miniprontuario_backend.model.Dentist;
import com.miniprontuario.miniprontuario_backend.model.Patient;
import com.miniprontuario.miniprontuario_backend.model.Procedure;
import com.miniprontuario.miniprontuario_backend.repository.PatientRepository;
import com.miniprontuario.miniprontuario_backend.repository.ProcedureRepository;
import com.miniprontuario.miniprontuario_backend.security.DentistPrincipal;
import com.miniprontuario.miniprontuario_backend.service.ProcedureService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
public class ProcedureServiceTest {

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private ProcedureService procedureService;

    private DentistPrincipal principal;
    private Dentist dentist;
    private Patient patient;

    @BeforeEach
    void setUp() {
        UUID dentistId = UUID.randomUUID();
        principal = new DentistPrincipal(dentistId, "dentist@example.com");
        dentist = Dentist.builder().name("Dr. Bob").email("dentist@example.com").build();
        dentist.setId(dentistId);

        patient = Patient.builder()
                .dentist(dentist)
                .name("John Doe")
                .cpf("98765432100")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        patient.setId(UUID.randomUUID());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, null
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createProcedure_ShouldCreateProcedure_WhenValidRequest() {
        ProcedureRequest request = ProcedureRequest.builder()
                .date(LocalDate.now())
                .description("Restoration")
                .tooth("11")
                .notes("Standard cavity restoration")
                .build();

        when(patientRepository.findByIdAndDentistId(patient.getId(), principal.getId()))
                .thenReturn(Optional.of(patient));

        Procedure savedProcedure = Procedure.builder()
                .patient(patient)
                .date(request.getDate())
                .description(request.getDescription())
                .tooth(request.getTooth())
                .notes(request.getNotes())
                .build();
        savedProcedure.setId(UUID.randomUUID());

        when(procedureRepository.save(any(Procedure.class))).thenReturn(savedProcedure);

        ProcedureResponse response = procedureService.createProcedure(patient.getId(), request);

        assertNotNull(response);
        assertEquals("Restoration", response.getDescription());
        assertEquals("11", response.getTooth());
        verify(procedureRepository, times(1)).save(any(Procedure.class));
    }

    @Test
    void createProcedure_ShouldThrowException_WhenDateInFuture() {
        ProcedureRequest request = ProcedureRequest.builder()
                .date(LocalDate.now().plusDays(1)) // future date
                .description("Restoration")
                .build();

        when(patientRepository.findByIdAndDentistId(patient.getId(), principal.getId()))
                .thenReturn(Optional.of(patient));

        assertThrows(BusinessException.class, () -> procedureService.createProcedure(patient.getId(), request));
        verify(procedureRepository, never()).save(any());
    }

    @Test
    void updateProcedure_ShouldUpdateProcedure_WhenWithin24Hours() {
        UUID procedureId = UUID.randomUUID();
        ProcedureRequest request = ProcedureRequest.builder()
                .date(LocalDate.now())
                .description("Updated Description")
                .build();

        Procedure existing = Procedure.builder()
                .patient(patient)
                .date(LocalDate.now().minusDays(1))
                .description("Old Description")
                .build();
        existing.setId(procedureId);
        existing.setCreatedAt(LocalDateTime.now().minusHours(23)); // within 24h

        when(procedureRepository.findByIdAndPatientDentistId(procedureId, principal.getId()))
                .thenReturn(Optional.of(existing));
        when(procedureRepository.save(any(Procedure.class))).thenReturn(existing);

        ProcedureResponse response = procedureService.updateProcedure(procedureId, request);

        assertNotNull(response);
        assertEquals("Updated Description", response.getDescription());
        verify(procedureRepository, times(1)).save(any(Procedure.class));
    }

    @Test
    void updateProcedure_ShouldThrowException_WhenAfter24Hours() {
        UUID procedureId = UUID.randomUUID();
        ProcedureRequest request = ProcedureRequest.builder()
                .date(LocalDate.now())
                .description("Updated Description")
                .build();

        Procedure existing = Procedure.builder()
                .patient(patient)
                .date(LocalDate.now().minusDays(2))
                .description("Old Description")
                .build();
        existing.setId(procedureId);
        existing.setCreatedAt(LocalDateTime.now().minusHours(25)); // older than 24h

        when(procedureRepository.findByIdAndPatientDentistId(procedureId, principal.getId()))
                .thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> procedureService.updateProcedure(procedureId, request));
        verify(procedureRepository, never()).save(any());
    }

    @Test
    void listProcedures_ShouldReturnProcedures() {
        when(patientRepository.findByIdAndDentistId(patient.getId(), principal.getId()))
                .thenReturn(Optional.of(patient));

        Procedure procedure = Procedure.builder()
                .patient(patient)
                .date(LocalDate.now())
                .description("Cleaning")
                .build();
        procedure.setId(UUID.randomUUID());

        when(procedureRepository.findByPatientIdAndPatientDentistId(patient.getId(), principal.getId()))
                .thenReturn(Collections.singletonList(procedure));

        List<ProcedureResponse> responses = procedureService.listProcedures(patient.getId());

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Cleaning", responses.get(0).getDescription());
    }
}

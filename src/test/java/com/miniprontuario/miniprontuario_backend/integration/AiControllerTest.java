package com.miniprontuario.miniprontuario_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniprontuario.miniprontuario_backend.dto.AuthDTOs.LoginRequest;
import com.miniprontuario.miniprontuario_backend.dto.AuthDTOs.RegisterRequest;
import com.miniprontuario.miniprontuario_backend.model.Patient;
import com.miniprontuario.miniprontuario_backend.model.Procedure;
import com.miniprontuario.miniprontuario_backend.repository.DentistRepository;
import com.miniprontuario.miniprontuario_backend.repository.PatientRepository;
import com.miniprontuario.miniprontuario_backend.repository.ProcedureRepository;
import com.miniprontuario.miniprontuario_backend.repository.RefreshTokenRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DentistRepository dentistRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String dentistToken;
    private UUID patientId;

    @BeforeEach
    void setUp() throws Exception {
        refreshTokenRepository.deleteAll();
        procedureRepository.hardDeleteAll();
        patientRepository.hardDeleteAll();
        dentistRepository.deleteAll();

        // 1. Register dentist
        RegisterRequest regRequest = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf("52998224725")
                .cro("SP-12345")
                .phone("11999999999")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        // 2. Login dentist to get JWT
        LoginRequest loginRequest = LoginRequest.builder()
                .email("gabriel@example.com")
                .password("password123")
                .build();

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        dentistToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // 3. Create a patient under this dentist
        UUID dentistId = UUID.fromString(objectMapper.readTree(loginResponse).get("user").get("id").asText());
        var dentist = dentistRepository.findById(dentistId).orElseThrow();

        Patient patient = Patient.builder()
                .dentist(dentist)
                .name("Maria Silva")
                .cpf("11144477735")
                .birthDate(LocalDate.of(1985, 5, 20))
                .phone("11988888888")
                .allergies("Penicilina")
                .systemicDiseases("Hipertensão")
                .medications("Losartana 50mg")
                .build();

        patient = patientRepository.save(patient);
        patientId = patient.getId();

        // 4. Create a procedure for the patient
        Procedure procedure = Procedure.builder()
                .patient(patient)
                .date(LocalDate.now())
                .description("Extração de dente siso")
                .tooth("18")
                .notes("Paciente com pressão controlada")
                .status("COMPLETED")
                .cost(BigDecimal.valueOf(350.00))
                .build();

        procedureRepository.save(procedure);
    }

    @Test
    void analyzePatient_ShouldReturnAnalysis_WhenAuthenticatedAndPatientExists() throws Exception {
        mockMvc.perform(post("/ai/analyze-patient/" + patientId)
                .header("Authorization", "Bearer " + dentistToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis").exists())
                .andExpect(jsonPath("$.analysis").value(org.hamcrest.Matchers.containsString("Relatório de Risco Clínico")))
                .andExpect(jsonPath("$.analysis").value(org.hamcrest.Matchers.containsString("Maria Silva")))
                .andExpect(jsonPath("$.analysis").value(org.hamcrest.Matchers.containsString("Penicilina")))
                .andExpect(jsonPath("$.analysis").value(org.hamcrest.Matchers.containsString("Hipertensão")));
    }

    @Test
    void analyzePatient_ShouldReturnNotFound_WhenPatientDoesNotExist() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(post("/ai/analyze-patient/" + randomId)
                .header("Authorization", "Bearer " + dentistToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void analyzePatient_ShouldReturnUnauthorized_WhenNoTokenProvided() throws Exception {
        mockMvc.perform(post("/ai/analyze-patient/" + patientId))
                .andExpect(status().isUnauthorized());
    }
}

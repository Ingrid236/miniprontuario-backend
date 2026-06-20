package com.miniprontuario.miniprontuario_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniprontuario.miniprontuario_backend.dto.ProcedureDTOs.*;
import com.miniprontuario.miniprontuario_backend.model.Dentist;
import com.miniprontuario.miniprontuario_backend.model.Patient;
import com.miniprontuario.miniprontuario_backend.model.Procedure;
import com.miniprontuario.miniprontuario_backend.repository.DentistRepository;
import com.miniprontuario.miniprontuario_backend.repository.PatientRepository;
import com.miniprontuario.miniprontuario_backend.repository.ProcedureRepository;
import com.miniprontuario.miniprontuario_backend.repository.RefreshTokenRepository;
import com.miniprontuario.miniprontuario_backend.security.JwtUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProcedureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DentistRepository dentistRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private Dentist dentist1;
    private Dentist dentist2;
    private Patient patient1;
    private Patient patient2;
    private String token1;
    private String token2;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        procedureRepository.hardDeleteAll();
        patientRepository.hardDeleteAll();
        dentistRepository.deleteAll();

        // Create Dentist 1 and Token 1
        dentist1 = Dentist.builder()
                .name("Dentist One")
                .email("one@example.com")
                .password("password")
                .cpf("11111111111")
                .cro("11111")
                .build();
        dentist1 = dentistRepository.save(dentist1);
        token1 = "Bearer " + jwtUtil.generateToken(dentist1.getId(), dentist1.getEmail());

        // Create Dentist 2 and Token 2
        dentist2 = Dentist.builder()
                .name("Dentist Two")
                .email("two@example.com")
                .password("password")
                .cpf("22222222222")
                .cro("22222")
                .build();
        dentist2 = dentistRepository.save(dentist2);
        token2 = "Bearer " + jwtUtil.generateToken(dentist2.getId(), dentist2.getEmail());

        // Create Patient 1 (under Dentist 1)
        patient1 = Patient.builder()
                .dentist(dentist1)
                .name("Patient One")
                .cpf("12345678901")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        patient1 = patientRepository.save(patient1);

        // Create Patient 2 (under Dentist 2)
        patient2 = Patient.builder()
                .dentist(dentist2)
                .name("Patient Two")
                .cpf("98765432109")
                .birthDate(LocalDate.of(1995, 5, 5))
                .build();
        patient2 = patientRepository.save(patient2);
    }

    @Test
    void createProcedure_ShouldCreate_WhenValidRequest() throws Exception {
        ProcedureRequest request = ProcedureRequest.builder()
                .date(LocalDate.now())
                .description("Restoration")
                .tooth("14")
                .notes("Cavity filled")
                .build();

        mockMvc.perform(post("/patients/" + patient1.getId() + "/procedures")
                .header("Authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Restoration"));

        assertFalse(procedureRepository.findByPatientIdAndPatientDentistId(patient1.getId(), dentist1.getId()).isEmpty());
    }

    @Test
    void createProcedure_ShouldReturnBadRequest_WhenDateInFuture() throws Exception {
        ProcedureRequest request = ProcedureRequest.builder()
                .date(LocalDate.now().plusDays(1)) // future
                .description("Restoration")
                .build();

        mockMvc.perform(post("/patients/" + patient1.getId() + "/procedures")
                .header("Authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProcedure_ShouldReturnNotFound_WhenPatientBelongsToAnotherDentist() throws Exception {
        ProcedureRequest request = ProcedureRequest.builder()
                .date(LocalDate.now())
                .description("Restoration")
                .build();

        // Dentist 1 trying to add procedure to Patient 2 (owned by Dentist 2)
        mockMvc.perform(post("/patients/" + patient2.getId() + "/procedures")
                .header("Authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listProcedures_ShouldReturnOnlyDentistOwnPatientProcedures() throws Exception {
        Procedure p1 = Procedure.builder().patient(patient1).date(LocalDate.now()).description("Restoration").build();
        procedureRepository.save(p1);

        mockMvc.perform(get("/patients/" + patient1.getId() + "/procedures")
                .header("Authorization", token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Restoration"));

        // Dentist 2 should not be able to list procedures for Patient 1
        mockMvc.perform(get("/patients/" + patient1.getId() + "/procedures")
                .header("Authorization", token2))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProcedure_ShouldUpdate_WhenWithin24Hours() throws Exception {
        Procedure p1 = Procedure.builder().patient(patient1).date(LocalDate.now()).description("Restoration").build();
        p1 = procedureRepository.save(p1);

        ProcedureRequest updateRequest = ProcedureRequest.builder()
                .date(LocalDate.now())
                .description("Updated Restoration")
                .build();

        mockMvc.perform(put("/procedures/" + p1.getId())
                .header("Authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Restoration"));
    }

    @Test
    void updateProcedure_ShouldReturnBadRequest_WhenAfter24Hours() throws Exception {
        Procedure p1 = Procedure.builder().patient(patient1).date(LocalDate.now()).description("Restoration").build();
        p1 = procedureRepository.save(p1);

        // Direct SQL update to shift created_at back by 25 hours
        jdbcTemplate.update("UPDATE procedure SET created_at = ? WHERE id = ?",
                LocalDateTime.now().minusHours(25), p1.getId());

        ProcedureRequest updateRequest = ProcedureRequest.builder()
                .date(LocalDate.now())
                .description("Updated Restoration")
                .build();

        mockMvc.perform(put("/procedures/" + p1.getId())
                .header("Authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }
}

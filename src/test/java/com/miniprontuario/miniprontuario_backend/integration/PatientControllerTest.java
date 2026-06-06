package com.miniprontuario.miniprontuario_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniprontuario.miniprontuario_backend.dto.PatientDTOs.PatientRequest;
import com.miniprontuario.miniprontuario_backend.model.Dentist;
import com.miniprontuario.miniprontuario_backend.model.Patient;
import com.miniprontuario.miniprontuario_backend.repository.DentistRepository;
import com.miniprontuario.miniprontuario_backend.repository.PatientRepository;
import com.miniprontuario.miniprontuario_backend.repository.RefreshTokenRepository;
import com.miniprontuario.miniprontuario_backend.security.JwtUtil;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PatientControllerTest {

    // Valid Brazilian CPFs for testing
    private static final String VALID_CPF_ALICE  = "52998224725"; // 529.982.247-25
    private static final String VALID_CPF_ALICE2 = "11144477735"; // 111.444.777-35
    private static final String VALID_CPF_BOB    = "87748248800"; // 877.482.488-00

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DentistRepository dentistRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private Dentist dentist1;
    private Dentist dentist2;
    private String token1;
    private String token2;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        patientRepository.hardDeleteAll();
        dentistRepository.deleteAll();

        dentist1 = Dentist.builder()
                .name("Dentist One")
                .email("one@example.com")
                .password("password")
                .cpf("11111111111")
                .cro("11111")
                .build();
        dentist1 = dentistRepository.save(dentist1);
        token1 = "Bearer " + jwtUtil.generateToken(dentist1.getId(), dentist1.getEmail());

        dentist2 = Dentist.builder()
                .name("Dentist Two")
                .email("two@example.com")
                .password("password")
                .cpf("22222222222")
                .cro("22222")
                .build();
        dentist2 = dentistRepository.save(dentist2);
        token2 = "Bearer " + jwtUtil.generateToken(dentist2.getId(), dentist2.getEmail());
    }

    @Test
    void registerPatient_ShouldCreatePatient_WhenValidRequest() throws Exception {
        PatientRequest request = PatientRequest.builder()
                .name("Alice Smith")
                .cpf(VALID_CPF_ALICE)
                .birthDate(LocalDate.of(1985, 5, 10))
                .phone("11988888888")
                .allergies("Penicillin")
                .systemicDiseases("Diabetes")
                .medications("Metformin 500mg")
                .build();

        mockMvc.perform(post("/patients")
                .header("Authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.medications").value("Metformin 500mg"));

        assertFalse(patientRepository.findByDentistId(dentist1.getId()).isEmpty());
    }

    @Test
    void registerPatient_ShouldReturnBadRequest_WhenCpfDuplicateForSameDentist() throws Exception {
        PatientRequest request = PatientRequest.builder()
                .name("Alice Smith")
                .cpf(VALID_CPF_ALICE)
                .birthDate(LocalDate.of(1985, 5, 10))
                .build();

        mockMvc.perform(post("/patients")
                .header("Authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Same CPF for same dentist -> Fail
        mockMvc.perform(post("/patients")
                .header("Authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerPatient_ShouldAllowSameCpf_ForDifferentDentists() throws Exception {
        PatientRequest request = PatientRequest.builder()
                .name("Alice Smith")
                .cpf(VALID_CPF_ALICE)
                .birthDate(LocalDate.of(1985, 5, 10))
                .build();

        mockMvc.perform(post("/patients")
                .header("Authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Same CPF for different dentist -> Allowed
        mockMvc.perform(post("/patients")
                .header("Authorization", token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void registerPatient_ShouldReturnBadRequest_WhenInvalidCpf() throws Exception {
        PatientRequest request = PatientRequest.builder()
                .name("Invalid CPF Patient")
                .cpf("12345678901") // digits don't pass verification algorithm
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        mockMvc.perform(post("/patients")
                .header("Authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerPatient_ShouldReturnBadRequest_WhenBirthDateTooOld() throws Exception {
        PatientRequest request = PatientRequest.builder()
                .name("Old Patient")
                .cpf(VALID_CPF_ALICE)
                .birthDate(LocalDate.of(1800, 1, 1)) // Age > 120 years
                .build();

        mockMvc.perform(post("/patients")
                .header("Authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listPatients_ShouldReturnOnlyDentistsOwnPatients() throws Exception {
        Patient p1 = Patient.builder().dentist(dentist1).name("Patient One").cpf("111").birthDate(LocalDate.now()).build();
        patientRepository.save(p1);

        Patient p2 = Patient.builder().dentist(dentist2).name("Patient Two").cpf("222").birthDate(LocalDate.now()).build();
        patientRepository.save(p2);

        mockMvc.perform(get("/patients")
                .header("Authorization", token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Patient One"));
    }

    @Test
    void getPatient_ShouldReturnNotFound_WhenPatientBelongsToAnotherDentist() throws Exception {
        Patient p2 = Patient.builder().dentist(dentist2).name("Patient Two").cpf("222").birthDate(LocalDate.now()).build();
        p2 = patientRepository.save(p2);

        mockMvc.perform(get("/patients/" + p2.getId())
                .header("Authorization", token1))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePatient_ShouldSoftDeletePatient() throws Exception {
        Patient p1 = Patient.builder().dentist(dentist1).name("Patient One").cpf("111").birthDate(LocalDate.now()).build();
        p1 = patientRepository.save(p1);

        mockMvc.perform(delete("/patients/" + p1.getId())
                .header("Authorization", token1))
                .andExpect(status().isNoContent());

        // Get should now return 404 because SQLRestriction deleted = false filters it out
        mockMvc.perform(get("/patients/" + p1.getId())
                .header("Authorization", token1))
                .andExpect(status().isNotFound());
    }
}

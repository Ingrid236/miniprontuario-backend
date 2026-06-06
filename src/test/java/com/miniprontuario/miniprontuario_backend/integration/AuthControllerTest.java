package com.miniprontuario.miniprontuario_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniprontuario.miniprontuario_backend.dto.AuthDTOs.LoginRequest;
import com.miniprontuario.miniprontuario_backend.dto.AuthDTOs.RegisterRequest;
import com.miniprontuario.miniprontuario_backend.repository.DentistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DentistRepository dentistRepository;

    @Autowired
    private com.miniprontuario.miniprontuario_backend.repository.PatientRepository patientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        patientRepository.hardDeleteAll();
        dentistRepository.deleteAll();
    }

    @Test
    void register_ShouldCreateDentist_WhenValidRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf("12345678901")
                .cro("12345")
                .phone("11999999999")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertTrue(dentistRepository.findByEmail("gabriel@example.com").isPresent());
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() throws Exception {
        RegisterRequest regRequest = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf("12345678901")
                .cro("12345")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = LoginRequest.builder()
                .email("gabriel@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenDuplicateEmail() throws Exception {
        RegisterRequest req1 = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf("12345678901")
                .cro("12345")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));

        RegisterRequest req2 = RegisterRequest.builder()
                .name("Dr. Gabriel Second")
                .email("gabriel@example.com")
                .password("password123")
                .cpf("12345678902")
                .cro("54321")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenDuplicateCpf() throws Exception {
        RegisterRequest req1 = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf("12345678901")
                .cro("12345")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));

        RegisterRequest req2 = RegisterRequest.builder()
                .name("Dr. Gabriel Second")
                .email("gabriel2@example.com")
                .password("password123")
                .cpf("12345678901")
                .cro("54321")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest());
    }
}

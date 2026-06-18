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
    private com.miniprontuario.miniprontuario_backend.repository.ProcedureRepository procedureRepository;

    @Autowired
    private com.miniprontuario.miniprontuario_backend.repository.RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Valid CPF: 529.982.247-25 (known valid Brazilian CPF for testing)
    private static final String VALID_CPF_1 = "52998224725";
    private static final String VALID_CPF_2 = "11144477735";
    // CRO: state prefix + digits
    private static final String VALID_CRO_1 = "SP-12345";
    private static final String VALID_CRO_2 = "RJ-54321";

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        procedureRepository.hardDeleteAll();
        patientRepository.hardDeleteAll();
        dentistRepository.deleteAll();
    }

    @Test
    void register_ShouldCreateDentist_WhenValidRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf(VALID_CPF_1)
                .cro(VALID_CRO_1)
                .phone("11999999999")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertTrue(dentistRepository.findByEmail("gabriel@example.com").isPresent());
    }

    @Test
    void login_ShouldReturnTokenPair_WhenValidCredentials() throws Exception {
        RegisterRequest regRequest = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf(VALID_CPF_1)
                .cro(VALID_CRO_1)
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
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.user.id").exists())
                .andExpect(jsonPath("$.user.name").value("Dr. Gabriel"))
                .andExpect(jsonPath("$.user.email").value("gabriel@example.com"));
    }

    @Test
    void refresh_ShouldReturnNewTokenPair_WhenValidRefreshToken() throws Exception {
        // Register + login to get initial tokens
        RegisterRequest regRequest = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf(VALID_CPF_1)
                .cro(VALID_CRO_1)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest.builder()
                        .email("gabriel@example.com")
                        .password("password123")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        // Use the refresh token to get a new pair
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void logout_ShouldRevokeRefreshToken() throws Exception {
        // Register + login
        RegisterRequest regRequest = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf(VALID_CPF_1)
                .cro(VALID_CRO_1)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest.builder()
                        .email("gabriel@example.com")
                        .password("password123")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        // Logout
        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isNoContent());

        // Attempting to use the same refresh token should fail
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenDuplicateEmail() throws Exception {
        RegisterRequest req1 = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf(VALID_CPF_1)
                .cro(VALID_CRO_1)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));

        RegisterRequest req2 = RegisterRequest.builder()
                .name("Dr. Gabriel Second")
                .email("gabriel@example.com")
                .password("password123")
                .cpf(VALID_CPF_2)
                .cro(VALID_CRO_2)
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
                .cpf(VALID_CPF_1)
                .cro(VALID_CRO_1)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));

        RegisterRequest req2 = RegisterRequest.builder()
                .name("Dr. Gabriel Second")
                .email("gabriel2@example.com")
                .password("password123")
                .cpf(VALID_CPF_1)
                .cro(VALID_CRO_2)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenDuplicateCro() throws Exception {
        RegisterRequest req1 = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf(VALID_CPF_1)
                .cro(VALID_CRO_1)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));

        RegisterRequest req2 = RegisterRequest.builder()
                .name("Dr. Gabriel Second")
                .email("gabriel2@example.com")
                .password("password123")
                .cpf(VALID_CPF_2)
                .cro(VALID_CRO_1)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenPasswordTooShort() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("short")
                .cpf(VALID_CPF_1)
                .cro(VALID_CRO_1)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenInvalidCroFormat() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf(VALID_CPF_1)
                .cro("INVALID-CRO-FORMAT")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMe_ShouldReturnProfile_WhenAuthenticated() throws Exception {
        RegisterRequest regRequest = RegisterRequest.builder()
                .name("Dr. Gabriel")
                .email("gabriel@example.com")
                .password("password123")
                .cpf(VALID_CPF_1)
                .cro(VALID_CRO_1)
                .phone("11999999999")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest.builder()
                        .email("gabriel@example.com")
                        .password("password123")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Dr. Gabriel"))
                .andExpect(jsonPath("$.email").value("gabriel@example.com"))
                .andExpect(jsonPath("$.cpf").value(VALID_CPF_1))
                .andExpect(jsonPath("$.cro").value(VALID_CRO_1.toUpperCase()))
                .andExpect(jsonPath("$.phone").value("11999999999"));
    }

    @Test
    void getMe_ShouldReturnUnauthorized_WhenNoToken() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_ShouldReturnUnauthorized_WhenInvalidToken() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me")
                .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isUnauthorized());
    }
}

package com.miniprontuario.miniprontuario_backend.service;

import com.miniprontuario.miniprontuario_backend.dto.AiAnalysisResponse;
import com.miniprontuario.miniprontuario_backend.exception.ResourceNotFoundException;
import com.miniprontuario.miniprontuario_backend.model.Patient;
import com.miniprontuario.miniprontuario_backend.model.Procedure;
import com.miniprontuario.miniprontuario_backend.repository.PatientRepository;
import com.miniprontuario.miniprontuario_backend.repository.ProcedureRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final PatientRepository patientRepository;
    private final ProcedureRepository procedureRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String apiKey;

    public AiAnalysisResponse analyzePatient(UUID patientId, UUID dentistId) {
        Patient patient = patientRepository.findByIdAndDentistId(patientId, dentistId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        List<Procedure> procedures = procedureRepository.findByPatientIdAndPatientDentistId(patientId, dentistId);
        int age = Period.between(patient.getBirthDate(), LocalDate.now()).getYears();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("Gemini API key is not configured. Falling back to local clinical rule-based analysis.");
            return new AiAnalysisResponse(getFallbackAnalysis(patient, age, procedures));
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

            String prompt = buildPrompt(patient, age, procedures);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> parts = Map.of("parts", List.of(textPart));
            Map<String, Object> requestBody = Map.of("contents", List.of(parts));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String text = extractTextFromResponse(response.getBody());
                if (text != null && !text.isBlank()) {
                    return new AiAnalysisResponse(text);
                }
            }
            
            log.warn("Gemini API returned empty response or error status. Falling back to local analysis.");
            return new AiAnalysisResponse(getFallbackAnalysis(patient, age, procedures));
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}. Falling back to local analysis.", e.getMessage(), e);
            return new AiAnalysisResponse(getFallbackAnalysis(patient, age, procedures));
        }
    }

    private String buildPrompt(Patient patient, int age, List<Procedure> procedures) {
        StringBuilder procBuilder = new StringBuilder();
        if (procedures.isEmpty()) {
            procBuilder.append("Nenhum procedimento registrado.");
        } else {
            for (Procedure proc : procedures) {
                procBuilder.append("- ").append(proc.getDate())
                           .append(": ").append(proc.getDescription());
                if (proc.getTooth() != null && !proc.getTooth().isBlank()) {
                    procBuilder.append(" (Dente ").append(proc.getTooth()).append(")");
                }
                procBuilder.append(", Status: ").append(proc.getStatus());
                if (proc.getCost() != null) {
                    procBuilder.append(", Custo: R$ ").append(proc.getCost());
                }
                procBuilder.append("\n");
            }
        }

        return "Você é um assistente de inteligência artificial clínica especializado em odontologia.\n" +
                "Analise a ficha clínica do paciente abaixo e gere um Relatório de Risco Clínico detalhado.\n" +
                "O relatório deve destacar estruturadamente em Markdown:\n" +
                "1. Riscos associados a doenças sistêmicas/crônicas.\n" +
                "2. Alergias a medicamentos ou substâncias (com foco especial em anestésicos locais, analgésicos e anti-inflamatórios).\n" +
                "3. Interações potenciais entre os medicamentos em uso e tratamentos odontológicos comuns.\n" +
                "4. Histórico de procedimentos já realizados e recomendações preventivas para os próximos procedimentos.\n\n" +
                "DADOS DO PACIENTE:\n" +
                "Nome: " + patient.getName() + "\n" +
                "Idade: " + age + " anos\n" +
                "Alergias: " + (patient.getAllergies() != null ? patient.getAllergies() : "Nenhuma") + "\n" +
                "Doenças Sistêmicas: " + (patient.getSystemicDiseases() != null ? patient.getSystemicDiseases() : "Nenhuma") + "\n" +
                "Medicamentos em uso: " + (patient.getMedications() != null ? patient.getMedications() : "Nenhum") + "\n\n" +
                "HISTÓRICO DE PROCEDIMENTOS:\n" +
                procBuilder.toString() + "\n\n" +
                "Gere um relatório amigável, focado em segurança do paciente, conciso e com marcações claras em Markdown.";
    }

    private String extractTextFromResponse(Map body) {
        try {
            List candidates = (List) body.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                if (content != null) {
                    List parts = (List) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map part = (Map) parts.get(0);
                        return (String) part.get("text");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini response payload", e);
        }
        return null;
    }

    private String getFallbackAnalysis(Patient patient, int age, List<Procedure> procedures) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Relatório de Risco Clínico (Regras Locais)\n\n");
        sb.append("> **Aviso**: Esta análise foi gerada localmente pelo sistema base.\n\n");
        sb.append("### 1. Resumo do Paciente\n");
        sb.append("- **Nome**: ").append(patient.getName()).append("\n");
        sb.append("- **Idade**: ").append(age).append(" anos\n");
        
        sb.append("\n### 2. Riscos de Alergias e Doenças Sistêmicas\n");
        boolean hasRisks = false;
        if (patient.getAllergies() != null && !patient.getAllergies().isBlank()) {
            sb.append("- **Alergias**: ").append(patient.getAllergies()).append(". *Atenção aos analgésicos e anti-inflamatórios.*\n");
            hasRisks = true;
        }
        if (patient.getSystemicDiseases() != null && !patient.getSystemicDiseases().isBlank()) {
            sb.append("- **Doenças**: ").append(patient.getSystemicDiseases()).append(". *Recomenda-se monitoramento cuidadoso.*\n");
            hasRisks = true;
        }
        if (patient.getMedications() != null && !patient.getMedications().isBlank()) {
            sb.append("- **Medicamentos**: ").append(patient.getMedications()).append(". *Verificar interações odontológicas.*\n");
            hasRisks = true;
        }
        if (!hasRisks) {
            sb.append("- Nenhuma alergia ou doença sistêmica relatada.\n");
        }
        
        sb.append("\n### 3. Recomendações Gerais\n");
        if (age > 60) {
            sb.append("- **Idoso**: Reduzir tempo de sessão e monitorar estresse.\n");
        }
        if (procedures.isEmpty()) {
            sb.append("- Recomendado realizar consulta inicial para avaliação clínica geral.\n");
        } else {
            sb.append("- Paciente possui ").append(procedures.size()).append(" procedimento(s) anteriores no prontuário.\n");
        }
        
        return sb.toString();
    }
}

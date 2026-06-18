package com.miniprontuario.miniprontuario_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniprontuario.miniprontuario_backend.dto.ProcessarConsultaRequestDTO;
import com.miniprontuario.miniprontuario_backend.dto.ProcessarConsultaResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class IaProntuarioService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final OpenAiAudioTranscriptionModel transcriptionModel;

    // Regex para identificar CPFs (com ou sem pontuação) e telefones
    private static final Pattern CPF_PATTERN = Pattern.compile("\\b(?:\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2})\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(?:\\(?\\d{2}\\)?\\s?)?(?:9?\\d{4}-?\\d{4})\\b");

    public IaProntuarioService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper, OpenAiAudioTranscriptionModel transcriptionModel) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.transcriptionModel = transcriptionModel;
    }

    public ProcessarConsultaResponseDTO processarTexto(ProcessarConsultaRequestDTO request) {
        if (Boolean.FALSE.equals(request.getConsentimentoEnvioIA())) {
            throw new IllegalArgumentException("Consentimento obrigatório: Os dados não podem ser processados pela IA sem consentimento explícito do usuário.");
        }

        String textoAnonimizado = anonimizarTexto(request.getTextoConsulta());

        try {
            return chamarIA(textoAnonimizado);
        } catch (Exception e) {
            log.error("Erro ao comunicar com a OpenAI. Utilizando fallback estático. Erro: {}", e.getMessage());
            return aplicarFallbackEstatico(textoAnonimizado);
        }
    }

    public String transcreverAudio(Resource audioFile) {
        try {
            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioFile);
            return transcriptionModel.call(prompt).getResult().getOutput();
        } catch (Exception e) {
            log.error("Erro ao transcrever o áudio com a OpenAI. Erro: {}", e.getMessage());
            throw new RuntimeException("Falha ao transcrever o áudio. O serviço pode estar indisponível.", e);
        }
    }

    private String anonimizarTexto(String textoBruto) {
        String textoSemCpf = CPF_PATTERN.matcher(textoBruto).replaceAll("[CPF MASCARADO]");
        return PHONE_PATTERN.matcher(textoSemCpf).replaceAll("[TELEFONE MASCARADO]");
    }

    private ProcessarConsultaResponseDTO chamarIA(String texto) {
        String template = """
            Você é um assistente médico especialista em processamento de linguagem natural.
            Leia o relato bruto do paciente e extraia as seguintes informações estruturadas:
            1. Um resumo curto e objetivo da queixa principal.
            2. Uma lista de sintomas mencionados.
            3. Especialidades médicas que deveriam avaliar este caso (ex: Cardiologia, Ortopedia).
            
            Relato: {relato}
            
            Retorne APENAS o JSON com os seguintes campos:
            - resumoQueixaPrincipal (string)
            - sintomas (array de strings)
            - especialidadesRecomendadas (array de strings)
            """;

        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("relato", texto));

        String respostaJson = chatClient.prompt(prompt).call().content();
        
        try {
            // Remove as marcações de bloco de código markdown se houver
            respostaJson = respostaJson.replace("```json", "").replace("```", "").trim();
            ProcessarConsultaResponseDTO response = objectMapper.readValue(respostaJson, ProcessarConsultaResponseDTO.class);
            response.setFallback(false);
            return response;
        } catch (JsonProcessingException e) {
            log.error("Falha ao fazer parse do JSON retornado pela IA", e);
            throw new RuntimeException("Falha no processamento da resposta da IA", e);
        }
    }

    private ProcessarConsultaResponseDTO aplicarFallbackEstatico(String texto) {
        String textoLower = texto.toLowerCase();
        List<String> sintomas = new ArrayList<>();
        List<String> especialidades = new ArrayList<>();

        if (textoLower.contains("dor") || textoLower.contains("doendo")) sintomas.add("Dor inespecífica");
        if (textoLower.contains("febre") || textoLower.contains("quente")) sintomas.add("Febre");
        if (textoLower.contains("pressão") || textoLower.contains("alta")) {
            sintomas.add("Alteração de Pressão");
            especialidades.add("Cardiologia");
        }
        if (textoLower.contains("osso") || textoLower.contains("fratura") || textoLower.contains("bater")) {
            especialidades.add("Ortopedia");
        }
        if (especialidades.isEmpty()) especialidades.add("Clínico Geral");

        return ProcessarConsultaResponseDTO.builder()
                .resumoQueixaPrincipal("Resumo gerado por fallback. Verifique o relato original.")
                .sintomas(sintomas)
                .especialidadesRecomendadas(especialidades)
                .isFallback(true)
                .build();
    }
}

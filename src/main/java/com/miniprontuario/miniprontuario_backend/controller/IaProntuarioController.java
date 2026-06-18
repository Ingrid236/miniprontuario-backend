package com.miniprontuario.miniprontuario_backend.controller;

import com.miniprontuario.miniprontuario_backend.dto.ProcessarConsultaRequestDTO;
import com.miniprontuario.miniprontuario_backend.dto.ProcessarConsultaResponseDTO;
import com.miniprontuario.miniprontuario_backend.service.IaProntuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ia")
@RequiredArgsConstructor
public class IaProntuarioController {

    private final IaProntuarioService iaProntuarioService;

    @PostMapping("/processar-consulta")
    public ResponseEntity<ProcessarConsultaResponseDTO> processarConsulta(
            @Valid @RequestBody ProcessarConsultaRequestDTO request) {
        
        ProcessarConsultaResponseDTO response = iaProntuarioService.processarTexto(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/transcrever-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> transcreverAudio(@RequestParam("audio") MultipartFile audio) {
        try {
            org.springframework.core.io.Resource audioResource = new org.springframework.core.io.ByteArrayResource(audio.getBytes()) {
                @Override
                public String getFilename() {
                    return audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "gravacao.m4a";
                }
            };
            String texto = iaProntuarioService.transcreverAudio(audioResource);
            return ResponseEntity.ok(texto);
        } catch (java.io.IOException e) {
            return ResponseEntity.status(500).body("Erro ao ler o arquivo de áudio");
        }
    }
}

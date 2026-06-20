package com.miniprontuario.miniprontuario_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessarConsultaRequestDTO {
    
    @NotBlank(message = "O texto da consulta não pode estar vazio")
    private String textoConsulta;
    
    @NotNull(message = "O consentimento para envio à IA é obrigatório")
    private Boolean consentimentoEnvioIA;
}

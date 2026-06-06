package com.miniprontuario.miniprontuario_backend.security;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DentistPrincipal {
    private final UUID id;
    private final String email;
}

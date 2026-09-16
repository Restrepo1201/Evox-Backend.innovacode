package com.evox.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/** Respuesta de login: token JWT + datos basicos del usuario. */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UsuarioBasico usuario;

    @Data
    @AllArgsConstructor
    public static class UsuarioBasico {
        private UUID id;
        private String nombre;
        private String rol;
    }
}
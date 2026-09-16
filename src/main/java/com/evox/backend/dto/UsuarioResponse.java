package com.evox.backend.dto;

import com.evox.backend.model.Perfil;
import com.evox.backend.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UsuarioResponse {
    private UUID id;
    private String nombreCompleto;
    private String correo;
    private Rol rol;

    public static UsuarioResponse desde(Perfil p) {
        return new UsuarioResponse(p.getId(), p.getNombreCompleto(), p.getCorreo(), p.getRol());
    }
}
package com.evox.backend.dto;

import com.evox.backend.model.Perfil;
import com.evox.backend.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Datos de un usuario para el panel de administracion. */
@Data
@AllArgsConstructor
public class UsuarioAdminResponse {
    private UUID id;
    private String nombreCompleto;
    private String correo;
    private Rol rol;
    private OffsetDateTime fechaRegistro;

    public static UsuarioAdminResponse desde(Perfil p) {
        return new UsuarioAdminResponse(p.getId(), p.getNombreCompleto(), p.getCorreo(), p.getRol(), p.getFechaCreacion());
    }
}
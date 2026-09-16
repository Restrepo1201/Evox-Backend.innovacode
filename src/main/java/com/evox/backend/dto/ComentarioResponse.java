package com.evox.backend.dto;

import com.evox.backend.model.Comentario;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ComentarioResponse {
    private UUID id;
    private String usuario;
    private Integer puntuacion;
    private String contenido;
    private OffsetDateTime fecha;

    public static ComentarioResponse desde(Comentario c) {
        return new ComentarioResponse(c.getId(), c.getUsuario().getNombreCompleto(),
                c.getPuntuacion(), c.getContenido(), c.getFechaCreacion());
    }
}
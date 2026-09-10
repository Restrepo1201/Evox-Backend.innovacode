package com.evox.backend.dto;

import com.evox.backend.model.Comentario;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ComentarioResponse {
    private Long id;
    private String usuario;
    private Integer puntuacion;
    private String contenido;
    private LocalDate fecha;

    public static ComentarioResponse desde(Comentario c) {
        return new ComentarioResponse(c.getId(), c.getUsuario().getNombre(),
                c.getPuntuacion(), c.getContenido(), c.getFecha());
    }
}

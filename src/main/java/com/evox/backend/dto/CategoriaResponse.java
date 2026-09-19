package com.evox.backend.dto;

import com.evox.backend.model.Categoria;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/** Datos de una categoria del catalogo. */
@Data
@AllArgsConstructor
public class CategoriaResponse {
    private UUID id;
    private String nombre;
    private String descripcion;
    private Integer orden;
    private Boolean activo;

    public static CategoriaResponse desde(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.getOrden(), c.getActivo());
    }
}
package com.evox.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Datos para comentar/calificar un producto: POST /api/v1/productos/{id}/comentarios */
@Data
public class ComentarioRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer puntuacion;

    @NotBlank
    private String contenido;
}

package com.evox.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

/** Datos para crear o actualizar un producto (POST y PUT /api/v1/productos). */
@Data
public class ProductoRequest {
    @NotBlank
    private String nombre;

    private String descripcion;

    @NotNull
    @PositiveOrZero
    private BigDecimal precio;

    @NotNull
    @PositiveOrZero
    private Integer stock;

    private String imagen;

    // El contrato de API usa "categoriaId", aqui lo guardamos directamente
    // como el nombre/texto de la categoria para simplificar el modelo.
    private String categoriaId;
}

package com.evox.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/** Datos para crear o actualizar un producto. */
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

    private UUID categoriaId;
}
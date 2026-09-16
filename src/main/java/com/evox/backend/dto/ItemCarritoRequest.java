package com.evox.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

/** Datos para agregar un producto al carrito: POST /api/v1/carrito/items */
@Data
public class ItemCarritoRequest {
    @NotNull
    private UUID productoId;

    @NotNull
    @Positive
    private Integer cantidad;
}
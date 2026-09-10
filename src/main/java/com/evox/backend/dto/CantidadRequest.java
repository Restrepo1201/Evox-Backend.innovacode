package com.evox.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** Datos para actualizar la cantidad de un item: PUT /api/v1/carrito/items/{id} */
@Data
public class CantidadRequest {
    @NotNull
    @Positive
    private Integer cantidad;
}

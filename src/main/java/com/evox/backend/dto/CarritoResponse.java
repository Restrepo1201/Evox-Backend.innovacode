package com.evox.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** Respuesta de GET /api/v1/carrito */
@Data
@AllArgsConstructor
public class CarritoResponse {
    private List<ItemCarritoResponse> items;
    private BigDecimal total;
}

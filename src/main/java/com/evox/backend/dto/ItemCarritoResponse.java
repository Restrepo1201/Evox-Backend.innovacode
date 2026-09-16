package com.evox.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ItemCarritoResponse {
    private UUID productoId;
    private String nombre;
    private BigDecimal precio;
    private Integer cantidad;
    private BigDecimal subtotal;
}
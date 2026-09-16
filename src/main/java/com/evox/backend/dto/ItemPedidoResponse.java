package com.evox.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ItemPedidoResponse {
    private UUID productoId;
    private String nombre;
    private Integer cantidad;
    private BigDecimal precio;
}
package com.evox.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ItemPedidoResponse {
    private Long productoId;
    private String nombre;
    private Integer cantidad;
    private BigDecimal precio;
}

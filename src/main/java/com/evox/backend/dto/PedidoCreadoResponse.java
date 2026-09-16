package com.evox.backend.dto;

import com.evox.backend.model.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PedidoCreadoResponse {
    private UUID id;
    private EstadoPedido estado;
    private BigDecimal total;
    private String mensaje;
}
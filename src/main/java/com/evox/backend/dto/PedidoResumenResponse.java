package com.evox.backend.dto;

import com.evox.backend.model.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Una fila del historial de pedidos: GET /api/v1/pedidos */
@Data
@AllArgsConstructor
public class PedidoResumenResponse {
    private UUID id;
    private OffsetDateTime fecha;
    private BigDecimal total;
    private EstadoPedido estado;
}
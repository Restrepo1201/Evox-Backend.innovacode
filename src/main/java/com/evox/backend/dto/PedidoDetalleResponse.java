package com.evox.backend.dto;

import com.evox.backend.model.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Detalle completo de un pedido: GET /api/v1/pedidos/{id} */
@Data
@AllArgsConstructor
public class PedidoDetalleResponse {
    private UUID id;
    private EstadoPedido estado;
    private BigDecimal total;
    private List<ItemPedidoResponse> items;
}
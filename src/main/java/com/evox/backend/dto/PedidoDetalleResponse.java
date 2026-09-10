package com.evox.backend.dto;

import com.evox.backend.model.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** Detalle completo de un pedido: GET /api/v1/pedidos/{id} */
@Data
@AllArgsConstructor
public class PedidoDetalleResponse {
    private Long id;
    private EstadoPedido estado;
    private BigDecimal total;
    private List<ItemPedidoResponse> items;
}

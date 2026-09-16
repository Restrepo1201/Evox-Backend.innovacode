package com.evox.backend.model;

/** Estados de un pedido (coinciden con el CHECK de la tabla pedidos). */
public enum EstadoPedido {
    PENDIENTE,
    PAGADO,
    ENVIADO,
    ENTREGADO
}
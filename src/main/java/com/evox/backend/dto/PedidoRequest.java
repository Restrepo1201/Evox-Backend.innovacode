package com.evox.backend.dto;

import lombok.Data;

/** Datos para crear un pedido: POST /api/v1/pedidos. El carrito se convierte en pedido. */
@Data
public class PedidoRequest {
    private String nota;
}
package com.evox.backend.dto;

import com.evox.backend.model.MetodoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Datos para crear un pedido a partir del carrito: POST /api/v1/pedidos */
@Data
public class PedidoRequest {
    @NotBlank
    private String direccionEntrega;

    @NotBlank
    private String ciudad;

    @NotNull
    private MetodoPago metodoPago;
}

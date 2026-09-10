package com.evox.backend.dto;

import com.evox.backend.model.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PedidoCreadoResponse {
    private Long id;
    private EstadoPedido estado;
    private BigDecimal total;
    private String mensaje;
}

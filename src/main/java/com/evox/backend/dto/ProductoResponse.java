package com.evox.backend.dto;

import com.evox.backend.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/** Representa un producto tal como se envia al frontend. */
@Data
@AllArgsConstructor
public class ProductoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String imagen;
    private String categoria;

    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(p.getId(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getStock(), p.getImagen(), p.getCategoria());
    }
}

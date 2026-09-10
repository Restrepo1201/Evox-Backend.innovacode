package com.evox.backend.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Una linea del carrito: un producto y la cantidad elegida.
 * Tabla: item_carrito
 */
@Entity
@Table(name = "item_carrito")
@Data
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "carrito_id")
    private Carrito carrito;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;
}

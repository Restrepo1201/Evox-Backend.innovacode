package com.evox.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Una linea del carrito de compras: cada fila es un producto con su cantidad.
 * Tabla: carritos
 */
@Entity
@Table(name = "carritos")
@Data
public class Carrito {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Perfil usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad = 1;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    private OffsetDateTime fecha;

    @PrePersist
    void prePersist() {
        if (fecha == null) {
            fecha = OffsetDateTime.now();
        }
    }
}
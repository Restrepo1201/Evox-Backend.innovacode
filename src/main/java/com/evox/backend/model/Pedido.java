package com.evox.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Un pedido confirmado. Los items se guardan como JSON en la columna items.
 * Tabla: pedidos
 */
@Entity
@Table(name = "pedidos")
@Data
public class Pedido {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Perfil usuario;

    private OffsetDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<ItemPedidoJson> items = new ArrayList<>();

    private String nota;

    @Column(name = "fecha_actualizacion")
    private OffsetDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        OffsetDateTime ahora = OffsetDateTime.now();
        if (fecha == null) {
            fecha = ahora;
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = ahora;
        }
    }

    /** Item de pedido tal como se guarda dentro del JSONB de la columna items. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemPedidoJson {
        private UUID productoId;
        private String nombre;
        private Integer cantidad;
        private BigDecimal precio;
    }
}
package com.evox.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Comentario + calificacion de un producto. Tabla: comentarios
 */
@Entity
@Table(name = "comentarios")
@Data
public class Comentario {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Perfil usuario;

    @Column(nullable = false)
    private Integer puntuacion;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }
    }
}
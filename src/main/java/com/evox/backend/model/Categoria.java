package com.evox.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Categoria del catalogo. Tabla: categorias
 */
@Entity
@Table(name = "categorias")
@Data
public class Categoria {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    private Integer orden = 0;

    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }
    }
}
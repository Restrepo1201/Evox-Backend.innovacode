package com.evox.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Perfil de usuario. Tabla: perfiles
 */
@Entity
@Table(name = "perfiles")
@Data
public class Perfil {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol = Rol.CLIENTE;

    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private OffsetDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        OffsetDateTime ahora = OffsetDateTime.now();
        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = ahora;
        }
    }
}
package com.evox.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Comentario + calificacion
 */
@Entity
@Table(name = "comentario")
@Data
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenido;

    @Column(nullable = false)
    private Integer puntuacion;

    @Column(nullable = false)
    private LocalDate fecha = LocalDate.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;
}

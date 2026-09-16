package com.evox.backend.repository;

import com.evox.backend.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ComentarioRepository extends JpaRepository<Comentario, UUID> {
    List<Comentario> findByProductoIdOrderByFechaCreacionDesc(UUID productoId);
}
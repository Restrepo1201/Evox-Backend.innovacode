package com.evox.backend.repository;

import com.evox.backend.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarritoRepository extends JpaRepository<Carrito, UUID> {
    List<Carrito> findByUsuarioIdOrderByFechaDesc(UUID usuarioId);
    Optional<Carrito> findByUsuarioIdAndProductoId(UUID usuarioId, UUID productoId);
    void deleteByUsuarioId(UUID usuarioId);
    void deleteByUsuarioIdAndProductoId(UUID usuarioId, UUID productoId);
}
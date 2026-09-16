package com.evox.backend.repository;

import com.evox.backend.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    // El filtro de categoria baja al nombre de la categoria relacionada (producto.categoria.nombre).
    Page<Producto> findByCategoria_NombreContainingIgnoreCaseAndNombreContainingIgnoreCase(
            String categoria, String nombre, Pageable pageable);

    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    Page<Producto> findByCategoria_NombreContainingIgnoreCase(String categoria, Pageable pageable);
}
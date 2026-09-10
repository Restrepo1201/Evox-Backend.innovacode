package com.evox.backend.repository;

import com.evox.backend.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Filtra por categoria (si viene) y por nombre que contenga el texto buscado (si viene).
    Page<Producto> findByCategoriaContainingIgnoreCaseAndNombreContainingIgnoreCase(
            String categoria, String nombre, Pageable pageable);

    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    Page<Producto> findByCategoriaContainingIgnoreCase(String categoria, Pageable pageable);
}

package com.evox.backend.repository;

import com.evox.backend.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerfilRepository extends JpaRepository<Perfil, UUID> {
    Optional<Perfil> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}
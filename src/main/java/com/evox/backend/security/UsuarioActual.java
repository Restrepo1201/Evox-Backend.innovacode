package com.evox.backend.security;

import com.evox.backend.model.Perfil;
import com.evox.backend.service.PerfilService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioActual {

    private final PerfilService perfilService;

    public UsuarioActual(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    public Perfil obtener() {
        String correo = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return perfilService.obtenerPorCorreo(correo);
    }
}
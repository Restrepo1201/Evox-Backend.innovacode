package com.evox.backend.security;

import com.evox.backend.model.Usuario;
import com.evox.backend.service.UsuarioService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioActual {

    private final UsuarioService usuarioService;

    public UsuarioActual(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public Usuario obtener() {
        String correo = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return usuarioService.obtenerPorCorreo(correo);
    }
}

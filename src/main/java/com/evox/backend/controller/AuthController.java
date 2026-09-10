package com.evox.backend.controller;

import com.evox.backend.dto.LoginRequest;
import com.evox.backend.dto.LoginResponse;
import com.evox.backend.dto.RegisterRequest;
import com.evox.backend.dto.UsuarioResponse;
import com.evox.backend.model.Usuario;
import com.evox.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Endpoints de autenticacion: registro e inicio de sesion. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // POST /api/v1/auth/register
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegisterRequest datos) {
        Usuario usuario = usuarioService.registrar(datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(usuario));
    }

    // POST /api/v1/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(@Valid @RequestBody LoginRequest datos) {
        return ResponseEntity.ok(usuarioService.iniciarSesion(datos));
    }
}

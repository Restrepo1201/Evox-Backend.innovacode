package com.evox.backend.controller;

import com.evox.backend.dto.ComentarioRequest;
import com.evox.backend.dto.ComentarioResponse;
import com.evox.backend.security.UsuarioActual;
import com.evox.backend.service.ComentarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints de comentarios y calificaciones de productos. */
@RestController
@RequestMapping("/api/v1/productos/{productoId}/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final UsuarioActual usuarioActual;

    public ComentarioController(ComentarioService comentarioService, UsuarioActual usuarioActual) {
        this.comentarioService = comentarioService;
        this.usuarioActual = usuarioActual;
    }

    // POST /api/v1/productos/{productoId}/comentarios (requiere estar autenticado)
    @PostMapping
    public ResponseEntity<ComentarioResponse> crear(@PathVariable Long productoId,
                                                      @Valid @RequestBody ComentarioRequest datos) {
        ComentarioResponse creado = comentarioService.crear(usuarioActual.obtener(), productoId, datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // GET /api/v1/productos/{productoId}/comentarios (publico)
    @GetMapping
    public ResponseEntity<List<ComentarioResponse>> listar(@PathVariable Long productoId) {
        return ResponseEntity.ok(comentarioService.listarDeProducto(productoId));
    }
}

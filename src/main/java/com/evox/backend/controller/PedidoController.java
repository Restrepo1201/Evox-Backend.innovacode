package com.evox.backend.controller;

import com.evox.backend.dto.*;
import com.evox.backend.security.UsuarioActual;
import com.evox.backend.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints de pedidos (requieren estar autenticado). */
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioActual usuarioActual;

    public PedidoController(PedidoService pedidoService, UsuarioActual usuarioActual) {
        this.pedidoService = pedidoService;
        this.usuarioActual = usuarioActual;
    }

    // POST /api/v1/pedidos
    @PostMapping
    public ResponseEntity<PedidoCreadoResponse> crear(@Valid @RequestBody PedidoRequest datos) {
        PedidoCreadoResponse creado = pedidoService.crearPedido(usuarioActual.obtener(), datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // GET /api/v1/pedidos
    @GetMapping
    public ResponseEntity<List<PedidoResumenResponse>> listar() {
        return ResponseEntity.ok(pedidoService.listarDe(usuarioActual.obtener()));
    }

    // GET /api/v1/pedidos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PedidoDetalleResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerDetalle(usuarioActual.obtener(), id));
    }
}

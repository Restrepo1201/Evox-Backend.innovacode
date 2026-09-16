package com.evox.backend.controller;

import com.evox.backend.dto.CatalogoResponse;
import com.evox.backend.dto.MensajeResponse;
import com.evox.backend.dto.ProductoRequest;
import com.evox.backend.dto.ProductoResponse;
import com.evox.backend.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Endpoints del catalogo de productos (publicos para consultar, ADMIN para modificar). */
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET /api/v1/productos?categoria=&buscar=&pagina=&limite=
    @GetMapping
    public ResponseEntity<CatalogoResponse> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String buscar,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(productoService.listar(categoria, buscar, pagina, limite));
    }

    // GET /api/v1/productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    // POST /api/v1/productos (ADMINISTRADOR)
    @PostMapping
    public ResponseEntity<MensajeResponse> crear(@Valid @RequestBody ProductoRequest datos) {
        productoService.crear(datos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeResponse("Producto creado correctamente"));
    }

    // PUT /api/v1/productos/{id} (ADMINISTRADOR)
    @PutMapping("/{id}")
    public ResponseEntity<MensajeResponse> actualizar(@PathVariable UUID id, @Valid @RequestBody ProductoRequest datos) {
        productoService.actualizar(id, datos);
        return ResponseEntity.ok(new MensajeResponse("Producto actualizado correctamente"));
    }

    // DELETE /api/v1/productos/{id} (ADMINISTRADOR)
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable UUID id) {
        productoService.eliminar(id);
        return ResponseEntity.ok(new MensajeResponse("Producto eliminado correctamente"));
    }
}
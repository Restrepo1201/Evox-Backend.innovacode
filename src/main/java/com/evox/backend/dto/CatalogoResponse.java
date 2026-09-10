package com.evox.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** Respuesta paginada del catalogo: GET /api/v1/productos */
@Data
@AllArgsConstructor
public class CatalogoResponse {
    private int pagina;
    private long total;
    private List<ProductoResponse> productos;
}

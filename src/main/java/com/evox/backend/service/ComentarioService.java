package com.evox.backend.service;

import com.evox.backend.dto.ComentarioRequest;
import com.evox.backend.dto.ComentarioResponse;
import com.evox.backend.model.Comentario;
import com.evox.backend.model.Producto;
import com.evox.backend.model.Usuario;
import com.evox.backend.repository.ComentarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ProductoService productoService;

    public ComentarioService(ComentarioRepository comentarioRepository, ProductoService productoService) {
        this.comentarioRepository = comentarioRepository;
        this.productoService = productoService;
    }

    /** POST /api/v1/productos/{productoId}/comentarios */
    public ComentarioResponse crear(Usuario usuario, Long productoId, ComentarioRequest datos) {
        Producto producto = productoService.buscarEntidad(productoId); // valida que exista (404 si no)

        Comentario comentario = new Comentario();
        comentario.setUsuario(usuario);
        comentario.setProducto(producto);
        comentario.setPuntuacion(datos.getPuntuacion());
        comentario.setContenido(datos.getContenido());

        comentario = comentarioRepository.save(comentario);
        return ComentarioResponse.desde(comentario);
    }

    /** GET /api/v1/productos/{productoId}/comentarios */
    public List<ComentarioResponse> listarDeProducto(Long productoId) {
        productoService.buscarEntidad(productoId); // valida que exista (404 si no)
        return comentarioRepository.findByProductoIdOrderByFechaDesc(productoId).stream()
                .map(ComentarioResponse::desde)
                .toList();
    }
}

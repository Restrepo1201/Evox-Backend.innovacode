package com.evox.backend.service;

import com.evox.backend.dto.CatalogoResponse;
import com.evox.backend.dto.ProductoRequest;
import com.evox.backend.dto.ProductoResponse;
import com.evox.backend.exception.ApiException;
import com.evox.backend.model.Categoria;
import com.evox.backend.model.Producto;
import com.evox.backend.repository.CategoriaRepository;
import com.evox.backend.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    /** GET /api/v1/productos : catalogo con filtros opcionales y paginacion. */
    public CatalogoResponse listar(String categoria, String buscar, int pagina, int limite) {
        int paginaSegura = Math.max(pagina, 1);
        int limiteSeguro = (limite <= 0) ? 10 : limite;
        PageRequest pageRequest = PageRequest.of(paginaSegura - 1, limiteSeguro);

        boolean hayCategoria = categoria != null && !categoria.isBlank();
        boolean hayBusqueda = buscar != null && !buscar.isBlank();

        Page<Producto> resultado;
        if (hayCategoria && hayBusqueda) {
            resultado = productoRepository.findByCategoria_NombreContainingIgnoreCaseAndNombreContainingIgnoreCase(
                    categoria, buscar, pageRequest);
        } else if (hayCategoria) {
            resultado = productoRepository.findByCategoria_NombreContainingIgnoreCase(categoria, pageRequest);
        } else if (hayBusqueda) {
            resultado = productoRepository.findByNombreContainingIgnoreCase(buscar, pageRequest);
        } else {
            resultado = productoRepository.findAll(pageRequest);
        }

        List<ProductoResponse> productos = resultado.getContent().stream()
                .map(ProductoResponse::desde)
                .toList();

        return new CatalogoResponse(paginaSegura, resultado.getTotalElements(), productos);
    }

    /** GET /api/v1/productos/{id} */
    public ProductoResponse obtenerPorId(UUID id) {
        return ProductoResponse.desde(buscarEntidad(id));
    }

    /** Metodo interno reutilizado por otros servicios (carrito, pedidos, comentarios). */
    public Producto buscarEntidad(UUID id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    /** POST /api/v1/productos (solo ADMINISTRADOR) */
    public UUID crear(ProductoRequest datos) {
        Producto producto = new Producto();
        copiarDatos(producto, datos);
        producto = productoRepository.save(producto);
        return producto.getId();
    }

    /** PUT /api/v1/productos/{id} (solo ADMINISTRADOR) */
    public void actualizar(UUID id, ProductoRequest datos) {
        Producto producto = buscarEntidad(id);
        copiarDatos(producto, datos);
        productoRepository.save(producto);
    }

    /** DELETE /api/v1/productos/{id} (solo ADMINISTRADOR) */
    public void eliminar(UUID id) {
        if (!productoRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        productoRepository.deleteById(id);
    }

    private void copiarDatos(Producto producto, ProductoRequest datos) {
        producto.setNombre(datos.getNombre());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setStock(datos.getStock());
        producto.setImagen(datos.getImagen());

        if (datos.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(datos.getCategoriaId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Categoria no encontrada"));
            producto.setCategoria(categoria);
        } else {
            producto.setCategoria(null);
        }
    }
}
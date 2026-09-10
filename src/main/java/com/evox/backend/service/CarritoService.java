package com.evox.backend.service;

import com.evox.backend.dto.CarritoResponse;
import com.evox.backend.dto.ItemCarritoResponse;
import com.evox.backend.exception.ApiException;
import com.evox.backend.model.Carrito;
import com.evox.backend.model.ItemCarrito;
import com.evox.backend.model.Producto;
import com.evox.backend.model.Usuario;
import com.evox.backend.repository.CarritoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoService productoService;

    public CarritoService(CarritoRepository carritoRepository, ProductoService productoService) {
        this.carritoRepository = carritoRepository;
        this.productoService = productoService;
    }

    /** Obtiene el carrito del usuario (todo cliente tiene uno, creado al registrarse). */
    public Carrito obtenerCarritoDe(Usuario usuario) {
        return carritoRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Carrito nuevo = new Carrito();
                    nuevo.setUsuario(usuario);
                    return carritoRepository.save(nuevo);
                });
    }

    /** GET /api/v1/carrito */
    public CarritoResponse verCarrito(Usuario usuario) {
        return construirRespuesta(obtenerCarritoDe(usuario));
    }

    /** POST /api/v1/carrito/items */
    public CarritoResponse agregarProducto(Usuario usuario, Long productoId, int cantidad) {
        Carrito carrito = obtenerCarritoDe(usuario);
        Producto producto = productoService.buscarEntidad(productoId);

        ItemCarrito item = carrito.getItems().stream()
                .filter(i -> i.getProducto().getId().equals(productoId))
                .findFirst()
                .orElse(null);

        int cantidadFinal = (item != null ? item.getCantidad() : 0) + cantidad;
        validarStock(producto, cantidadFinal);

        if (item == null) {
            item = new ItemCarrito();
            item.setCarrito(carrito);
            item.setProducto(producto);
            item.setCantidad(cantidad);
            carrito.getItems().add(item);
        } else {
            item.setCantidad(cantidadFinal);
        }

        carritoRepository.save(carrito);
        return construirRespuesta(carrito);
    }

    /** PUT /api/v1/carrito/items/{productoId} */
    public CarritoResponse actualizarCantidad(Usuario usuario, Long productoId, int cantidad) {
        Carrito carrito = obtenerCarritoDe(usuario);
        ItemCarrito item = buscarItem(carrito, productoId);

        validarStock(item.getProducto(), cantidad);
        item.setCantidad(cantidad);

        carritoRepository.save(carrito);
        return construirRespuesta(carrito);
    }

    /** DELETE /api/v1/carrito/items/{productoId} */
    public void eliminarProducto(Usuario usuario, Long productoId) {
        Carrito carrito = obtenerCarritoDe(usuario);
        ItemCarrito item = buscarItem(carrito, productoId);
        carrito.getItems().remove(item);
        carritoRepository.save(carrito);
    }

    private ItemCarrito buscarItem(Carrito carrito, Long productoId) {
        return carrito.getItems().stream()
                .filter(i -> i.getProducto().getId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El producto no esta en el carrito"));
    }

    private void validarStock(Producto producto, int cantidadDeseada) {
        if (cantidadDeseada > producto.getStock()) {
            throw new ApiException(HttpStatus.CONFLICT, "Stock insuficiente");
        }
    }

    private CarritoResponse construirRespuesta(Carrito carrito) {
        List<ItemCarritoResponse> items = carrito.getItems().stream()
                .map(i -> new ItemCarritoResponse(
                        i.getProducto().getId(),
                        i.getProducto().getNombre(),
                        i.getProducto().getPrecio(),
                        i.getCantidad(),
                        i.getProducto().getPrecio().multiply(BigDecimal.valueOf(i.getCantidad()))
                ))
                .toList();

        BigDecimal total = items.stream()
                .map(ItemCarritoResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CarritoResponse(items, total);
    }
}

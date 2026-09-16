package com.evox.backend.service;

import com.evox.backend.dto.ItemPedidoResponse;
import com.evox.backend.dto.PedidoCreadoResponse;
import com.evox.backend.dto.PedidoDetalleResponse;
import com.evox.backend.dto.PedidoRequest;
import com.evox.backend.dto.PedidoResumenResponse;
import com.evox.backend.exception.ApiException;
import com.evox.backend.model.Carrito;
import com.evox.backend.model.EstadoPedido;
import com.evox.backend.model.Pedido;
import com.evox.backend.model.Perfil;
import com.evox.backend.model.Producto;
import com.evox.backend.model.Rol;
import com.evox.backend.repository.CarritoRepository;
import com.evox.backend.repository.PedidoRepository;
import com.evox.backend.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final CarritoRepository carritoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository,
                         CarritoRepository carritoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.carritoRepository = carritoRepository;
    }

    /** POST /api/v1/pedidos : convierte el carrito actual del cliente en un pedido. */
    @Transactional
    public PedidoCreadoResponse crearPedido(Perfil usuario, PedidoRequest datos) {
        List<Carrito> lineas = carritoRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId());

        if (lineas.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El carrito esta vacio");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setNota(datos.getNota());

        BigDecimal total = BigDecimal.ZERO;

        for (Carrito linea : lineas) {
            Producto producto = linea.getProducto();

            if (linea.getCantidad() > producto.getStock()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Stock insuficiente para " + producto.getNombre());
            }

            // Se descuenta el stock vendido.
            producto.setStock(producto.getStock() - linea.getCantidad());
            productoRepository.save(producto);

            pedido.getItems().add(new Pedido.ItemPedidoJson(
                    producto.getId(), producto.getNombre(), linea.getCantidad(), linea.getPrecioUnitario()));

            total = total.add(linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad())));
        }

        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        // El carrito queda vacio despues de comprar.
        carritoRepository.deleteByUsuarioId(usuario.getId());

        return new PedidoCreadoResponse(pedido.getId(), pedido.getEstado(), pedido.getTotal(),
                "Pedido creado correctamente");
    }

    /** GET /api/v1/pedidos */
    public List<PedidoResumenResponse> listarDe(Perfil usuario) {
        return pedidoRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId()).stream()
                .map(p -> new PedidoResumenResponse(p.getId(), p.getFecha(), p.getTotal(), p.getEstado()))
                .toList();
    }

    /** GET /api/v1/pedidos/{id} : solo el dueno del pedido o un administrador pueden verlo. */
    public PedidoDetalleResponse obtenerDetalle(Perfil usuario, UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        boolean esDueno = pedido.getUsuario().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRol() == Rol.ADMINISTRADOR;

        if (!esDueno && !esAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tiene acceso a este pedido");
        }

        List<ItemPedidoResponse> items = pedido.getItems().stream()
                .map(i -> new ItemPedidoResponse(i.getProductoId(), i.getNombre(),
                        i.getCantidad(), i.getPrecio()))
                .toList();

        return new PedidoDetalleResponse(pedido.getId(), pedido.getEstado(), pedido.getTotal(), items);
    }
}
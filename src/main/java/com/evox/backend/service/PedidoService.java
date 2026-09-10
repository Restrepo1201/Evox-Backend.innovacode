package com.evox.backend.service;

import com.evox.backend.dto.*;
import com.evox.backend.exception.ApiException;
import com.evox.backend.model.*;
import com.evox.backend.repository.PedidoRepository;
import com.evox.backend.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final CarritoService carritoService;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository,
                          CarritoService carritoService) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.carritoService = carritoService;
    }

    /** POST /api/v1/pedidos : convierte el carrito actual del cliente en un pedido. */
    @Transactional
    public PedidoCreadoResponse crearPedido(Usuario usuario, PedidoRequest datos) {
        Carrito carrito = carritoService.obtenerCarritoDe(usuario);

        if (carrito.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El carrito esta vacio");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccionEntrega(datos.getDireccionEntrega());
        pedido.setCiudad(datos.getCiudad());
        pedido.setMetodoPago(datos.getMetodoPago());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        BigDecimal total = BigDecimal.ZERO;

        for (ItemCarrito itemCarrito : carrito.getItems()) {
            Producto producto = itemCarrito.getProducto();

            if (itemCarrito.getCantidad() > producto.getStock()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Stock insuficiente para " + producto.getNombre());
            }

            // Se descuenta el stock vendido.
            producto.setStock(producto.getStock() - itemCarrito.getCantidad());
            productoRepository.save(producto);

            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setPedido(pedido);
            itemPedido.setProducto(producto);
            itemPedido.setNombreProducto(producto.getNombre());
            itemPedido.setPrecioUnitario(producto.getPrecio());
            itemPedido.setCantidad(itemCarrito.getCantidad());
            pedido.getItems().add(itemPedido);

            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(itemCarrito.getCantidad())));
        }

        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        // El carrito queda vacio despues de comprar.
        carrito.getItems().clear();

        return new PedidoCreadoResponse(pedido.getId(), pedido.getEstado(), pedido.getTotal(),
                "Pedido creado correctamente");
    }

    /** GET /api/v1/pedidos */
    public List<PedidoResumenResponse> listarDe(Usuario usuario) {
        return pedidoRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId()).stream()
                .map(p -> new PedidoResumenResponse(p.getId(), p.getFecha(), p.getTotal(), p.getEstado()))
                .toList();
    }

    /** GET /api/v1/pedidos/{id} : solo el dueno del pedido o un administrador pueden verlo. */
    public PedidoDetalleResponse obtenerDetalle(Usuario usuario, Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        boolean esDueno = pedido.getUsuario().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRol() == Rol.ADMINISTRADOR;

        if (!esDueno && !esAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tiene acceso a este pedido");
        }

        List<ItemPedidoResponse> items = pedido.getItems().stream()
                .map(i -> new ItemPedidoResponse(i.getProducto().getId(), i.getNombreProducto(),
                        i.getCantidad(), i.getPrecioUnitario()))
                .toList();

        return new PedidoDetalleResponse(pedido.getId(), pedido.getEstado(), pedido.getTotal(), items);
    }
}

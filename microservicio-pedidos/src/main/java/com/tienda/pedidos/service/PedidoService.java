package com.tienda.pedidos.service;

import com.tienda.pedidos.client.ProductoClient;
import com.tienda.pedidos.client.dto.ProductoDTO;
import com.tienda.pedidos.dto.DetalleResponseDTO;
import com.tienda.pedidos.dto.ItemPedidoDTO;
import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.exception.BusinessException;
import com.tienda.pedidos.exception.ResourceNotFoundException;
import com.tienda.pedidos.model.DetallePedido;
import com.tienda.pedidos.model.Pedido;
import com.tienda.pedidos.model.PedidoEstado;
import com.tienda.pedidos.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de negocio para la gestion de pedidos (ventas).
 */
@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final ProductoClient productoClient;

    public PedidoService(PedidoRepository pedidoRepository, ProductoClient productoClient) {
        this.pedidoRepository = pedidoRepository;
        this.productoClient = productoClient;
    }

    /**
     * Registra un nuevo pedido.
     *
     * Flujo:
     * 1. Se valida el usuario y que existan items.
     * 2. Por cada item se consulta el producto en el microservicio de productos
     *    (el precio SIEMPRE proviene de ese servicio, nunca del cliente).
     * 3. Se reserva el stock de cada producto.
     * 4. Se calculan los subtotales y el total.
     * 5. Se guarda el pedido en estado PENDIENTE.
     *
     * Mecanismo de compensacion (base para un futuro patron Saga): si la reserva
     * de stock de un item falla despues de haber reservado items anteriores, se
     * liberan todas las reservas ya confirmadas antes de propagar la excepcion.
     * De este modo no quedan pedidos ni reservas de stock inconsistentes.
     */
    @Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO dto) {
        if (dto.getIdUsuario() == null) {
            throw new BusinessException("El usuario es obligatorio para registrar el pedido");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("El pedido debe contener al menos un producto");
        }

        List<Reserva> reservasRealizadas = new ArrayList<>();
        Pedido pedido = new Pedido();
        pedido.setIdUsuario(dto.getIdUsuario());
        pedido.setEstado(PedidoEstado.PENDIENTE);
        pedido.setStockDevuelto(Boolean.FALSE);

        try {
            for (ItemPedidoDTO item : dto.getItems()) {
                if (item.getCantidad() == null || item.getCantidad() <= 0) {
                    throw new BusinessException("La cantidad de cada producto debe ser mayor a cero");
                }

                ProductoDTO producto = productoClient.obtenerProducto(item.getIdProducto());
                if (producto == null) {
                    throw new BusinessException("Producto no encontrado con id " + item.getIdProducto());
                }
                if (Boolean.FALSE.equals(producto.getEstado())) {
                    throw new BusinessException("El producto " + item.getIdProducto() + " no esta disponible para la venta");
                }

                // Reserva de stock: si falla, se compensa con lo ya reservado.
                productoClient.reservarStock(item.getIdProducto(), item.getCantidad());
                reservasRealizadas.add(new Reserva(item.getIdProducto(), item.getCantidad()));

                BigDecimal precio = producto.getPrecio().setScale(2, RoundingMode.HALF_UP);
                BigDecimal subtotalDetalle = precio
                        .multiply(BigDecimal.valueOf(item.getCantidad()))
                        .setScale(2, RoundingMode.HALF_UP);

                DetallePedido detalle = new DetallePedido();
                detalle.setIdProducto(item.getIdProducto());
                detalle.setNombreProducto(producto.getNombre());
                detalle.setPrecioUnitario(precio);
                detalle.setCantidad(item.getCantidad());
                detalle.setSubtotal(subtotalDetalle);
                pedido.agregarDetalle(detalle);
            }
        } catch (RuntimeException ex) {
            // Compensacion: liberar el stock de las reservas ya confirmadas.
            compensarReservas(reservasRealizadas);
            throw ex;
        }

        BigDecimal subtotal = pedido.getDetalles().stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        pedido.setSubtotal(subtotal);
        // El total no incluye impuestos: es igual al subtotal.
        pedido.setTotal(subtotal);

        Pedido guardado = pedidoRepository.save(pedido);
        return mapear(guardado);
    }

    /**
     * Lista todos los pedidos, los mas recientes primero.
     */
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listar() {
        return pedidoRepository.findAllByOrderByFechaPedidoDesc().stream().map(this::mapear).toList();
    }

    /**
     * Obtiene un pedido por su identificador.
     */
    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPorId(Long id) {
        return mapear(buscarOFallar(id));
    }

    /**
     * Lista los pedidos de un usuario, los mas recientes primero.
     */
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorUsuario(Long idUsuario) {
        return pedidoRepository.findByIdUsuarioOrderByFechaPedidoDesc(idUsuario).stream().map(this::mapear).toList();
    }

    /**
     * Cambia el estado de un pedido aplicando las reglas de transicion.
     *
     * Regla clave: al cancelar un pedido (que no estaba cancelado y cuyo stock
     * no habia sido devuelto) se libera el stock de cada detalle y se marca
     * stockDevuelto = true, garantizando que el stock se devuelva una sola vez.
     */
    @Transactional
    public PedidoResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        Pedido pedido = buscarOFallar(id);

        PedidoEstado estadoDestino = parsearEstado(nuevoEstado);

        if (pedido.getEstado() == PedidoEstado.CANCELADO && estadoDestino != PedidoEstado.CANCELADO) {
            throw new BusinessException("Un pedido cancelado no puede cambiar a otro estado");
        }

        if (pedido.getEstado() == PedidoEstado.RECHAZADO && estadoDestino == PedidoEstado.PAGADO) {
            throw new BusinessException("Un pedido rechazado no puede pasar a estado PAGADO");
        }

        if (estadoDestino == PedidoEstado.CANCELADO
                && pedido.getEstado() != PedidoEstado.CANCELADO
                && !Boolean.TRUE.equals(pedido.getStockDevuelto())) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                productoClient.liberarStock(detalle.getIdProducto(), detalle.getCantidad());
            }
            pedido.setStockDevuelto(Boolean.TRUE);
        }

        pedido.setEstado(estadoDestino);
        return mapear(pedidoRepository.save(pedido));
    }

    // ----------------------------- Metodos de apoyo -----------------------------

    public Pedido buscarOFallar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id " + id));
    }

    /**
     * Libera el stock de las reservas confirmadas durante la creacion del pedido.
     */
    private void compensarReservas(List<Reserva> reservas) {
        for (Reserva reserva : reservas) {
            try {
                productoClient.liberarStock(reserva.idProducto(), reserva.cantidad());
            } catch (RuntimeException e) {
                log.warn("No se pudo liberar el stock del producto {} tras un fallo en la creacion del pedido",
                        reserva.idProducto(), e);
            }
        }
    }

    private PedidoEstado parsearEstado(String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new BusinessException("El estado es obligatorio. Valores permitidos: PENDIENTE, PAGADO, RECHAZADO, CANCELADO, ENTREGADO");
        }
        try {
            return PedidoEstado.valueOf(nuevoEstado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado invalido: " + nuevoEstado
                    + ". Valores permitidos: PENDIENTE, PAGADO, RECHAZADO, CANCELADO, ENTREGADO");
        }
    }

    private PedidoResponseDTO mapear(Pedido pedido) {
        List<DetalleResponseDTO> detalles = pedido.getDetalles().stream()
                .map(d -> new DetalleResponseDTO(
                        d.getIdDetalle(),
                        d.getIdProducto(),
                        d.getNombreProducto(),
                        d.getPrecioUnitario(),
                        d.getCantidad(),
                        d.getSubtotal()))
                .toList();
        return new PedidoResponseDTO(
                pedido.getIdPedido(),
                pedido.getIdUsuario(),
                pedido.getSubtotal(),
                pedido.getTotal(),
                pedido.getEstado(),
                pedido.getStockDevuelto(),
                pedido.getFechaPedido(),
                detalles);
    }

    /**
     * Reserva de stock confirmada, usada para la compensacion ante fallos.
     */
    private record Reserva(Long idProducto, Integer cantidad) {
    }
}

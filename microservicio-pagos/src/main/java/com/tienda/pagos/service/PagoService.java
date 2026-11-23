package com.tienda.pagos.service;

import com.tienda.pagos.client.PedidoClient;
import com.tienda.pagos.client.dto.PedidoResumenDTO;
import com.tienda.pagos.dto.PagoRequestDTO;
import com.tienda.pagos.dto.PagoResponseDTO;
import com.tienda.pagos.exception.BusinessException;
import com.tienda.pagos.exception.ResourceNotFoundException;
import com.tienda.pagos.model.EstadoPago;
import com.tienda.pagos.model.MetodoPago;
import com.tienda.pagos.model.Pago;
import com.tienda.pagos.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Reglas de negocio para la gestion de pagos simulados.
 */
@Service
public class PagoService {

    private static final String ESTADO_PEDIDO_CANCELADO = "CANCELADO";

    private final PagoRepository pagoRepository;
    private final PedidoClient pedidoClient;

    public PagoService(PagoRepository pagoRepository, PedidoClient pedidoClient) {
        this.pagoRepository = pagoRepository;
        this.pedidoClient = pedidoClient;
    }

    /**
     * Simula el pago de un pedido. El monto se obtiene siempre del total del pedido
     * consultado al microservicio de pedidos, nunca de datos enviados por el cliente.
     */
    @Transactional
    public PagoResponseDTO simularPago(PagoRequestDTO dto) {
        Long idPedido = dto.getIdPedido();

        PedidoResumenDTO pedido = pedidoClient.obtenerPedido(idPedido);
        if (pedido == null) {
            throw new BusinessException("El pedido con id " + idPedido + " no existe");
        }

        if (ESTADO_PEDIDO_CANCELADO.equalsIgnoreCase(pedido.getEstado())) {
            throw new BusinessException("No se puede pagar un pedido cancelado");
        }

        MetodoPago metodoPago = resolverMetodoPago(dto.getMetodoPago());
        EstadoPago estadoPago = determinarResultado(dto, metodoPago);

        Pago pago = new Pago();
        pago.setIdPedido(idPedido);
        pago.setMonto(pedido.getTotal());
        pago.setMetodoPago(metodoPago);
        pago.setEstadoPago(estadoPago);
        pago.setCodigoOperacion(generarCodigoOperacion());

        Pago guardado = pagoRepository.save(pago);

        notificarEstadoPedido(idPedido, estadoPago);

        return mapear(guardado);
    }

    /**
     * Obtiene un pago por su identificador.
     */
    @Transactional(readOnly = true)
    public PagoResponseDTO obtenerPorId(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id " + id));
        return mapear(pago);
    }

    /**
     * Lista los pagos asociados a un pedido, ordenados del mas reciente al mas antiguo.
     */
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> listarPorPedido(Long idPedido) {
        return pagoRepository.findByIdPedidoOrderByFechaPagoDesc(idPedido).stream().map(this::mapear).toList();
    }

    // ----------------------------- Metodos de apoyo -----------------------------

    private MetodoPago resolverMetodoPago(String metodoPago) {
        try {
            return MetodoPago.valueOf(metodoPago.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(
                    "Metodo de pago invalido: " + metodoPago
                            + ". Valores permitidos: EFECTIVO, YAPE, PLIN, TRANSFERENCIA, TARJETA_PRUEBA");
        }
    }

    /**
     * Determina el resultado simulado del pago aplicando las reglas de negocio.
     */
    private EstadoPago determinarResultado(PagoRequestDTO dto, MetodoPago metodoPago) {
        String resultado = dto.getSimularResultado();
        if (resultado != null && !resultado.isBlank()) {
            String normalizado = resultado.trim().toUpperCase();
            if ("APROBADO".equals(normalizado)) {
                return EstadoPago.APROBADO;
            }
            if ("RECHAZADO".equals(normalizado)) {
                return EstadoPago.RECHAZADO;
            }
        }

        if (metodoPago == MetodoPago.TARJETA_PRUEBA && esTarjetaRechazada(dto.getNumeroTarjeta())) {
            return EstadoPago.RECHAZADO;
        }

        return EstadoPago.APROBADO;
    }

    private boolean esTarjetaRechazada(String numeroTarjeta) {
        return numeroTarjeta != null && numeroTarjeta.trim().endsWith("0000");
    }

    private String generarCodigoOperacion() {
        return "OP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Notifica al microservicio de pedidos el nuevo estado segun el resultado del pago.
     * Regla: un pedido solo se marca como PAGADO si el pago fue aprobado.
     */
    private void notificarEstadoPedido(Long idPedido, EstadoPago estadoPago) {
        if (estadoPago == EstadoPago.APROBADO) {
            pedidoClient.actualizarEstado(idPedido, "PAGADO");
        } else if (estadoPago == EstadoPago.RECHAZADO) {
            pedidoClient.actualizarEstado(idPedido, "RECHAZADO");
        }
    }

    private PagoResponseDTO mapear(Pago pago) {
        return new PagoResponseDTO(
                pago.getIdPago(),
                pago.getIdPedido(),
                pago.getMonto(),
                pago.getMetodoPago(),
                pago.getEstadoPago(),
                pago.getCodigoOperacion(),
                pago.getFechaPago());
    }
}

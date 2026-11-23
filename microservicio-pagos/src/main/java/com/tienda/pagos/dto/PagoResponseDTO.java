package com.tienda.pagos.dto;

import com.tienda.pagos.model.EstadoPago;
import com.tienda.pagos.model.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos de salida de un pago.
 */
public class PagoResponseDTO {

    private Long idPago;
    private Long idPedido;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private EstadoPago estadoPago;
    private String codigoOperacion;
    private LocalDateTime fechaPago;

    public PagoResponseDTO() {
    }

    public PagoResponseDTO(Long idPago, Long idPedido, BigDecimal monto, MetodoPago metodoPago,
                           EstadoPago estadoPago, String codigoOperacion, LocalDateTime fechaPago) {
        this.idPago = idPago;
        this.idPedido = idPedido;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.estadoPago = estadoPago;
        this.codigoOperacion = codigoOperacion;
        this.fechaPago = fechaPago;
    }

    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(Long idPago) {
        this.idPago = idPago;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getCodigoOperacion() {
        return codigoOperacion;
    }

    public void setCodigoOperacion(String codigoOperacion) {
        this.codigoOperacion = codigoOperacion;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
}

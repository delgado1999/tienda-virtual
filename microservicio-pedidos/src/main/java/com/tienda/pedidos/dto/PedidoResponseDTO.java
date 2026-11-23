package com.tienda.pedidos.dto;

import com.tienda.pedidos.model.PedidoEstado;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos de salida de un pedido.
 */
public class PedidoResponseDTO {

    private Long idPedido;
    private Long idUsuario;
    private BigDecimal subtotal;
    private BigDecimal total;
    private PedidoEstado estado;
    private Boolean stockDevuelto;
    private LocalDateTime fechaPedido;
    private List<DetalleResponseDTO> detalles;

    public PedidoResponseDTO() {
    }

    public PedidoResponseDTO(Long idPedido, Long idUsuario, BigDecimal subtotal, BigDecimal total,
                             PedidoEstado estado, Boolean stockDevuelto, LocalDateTime fechaPedido,
                             List<DetalleResponseDTO> detalles) {
        this.idPedido = idPedido;
        this.idUsuario = idUsuario;
        this.subtotal = subtotal;
        this.total = total;
        this.estado = estado;
        this.stockDevuelto = stockDevuelto;
        this.fechaPedido = fechaPedido;
        this.detalles = detalles;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public PedidoEstado getEstado() {
        return estado;
    }

    public void setEstado(PedidoEstado estado) {
        this.estado = estado;
    }

    public Boolean getStockDevuelto() {
        return stockDevuelto;
    }

    public void setStockDevuelto(Boolean stockDevuelto) {
        this.stockDevuelto = stockDevuelto;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public List<DetalleResponseDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleResponseDTO> detalles) {
        this.detalles = detalles;
    }
}

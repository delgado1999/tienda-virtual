package com.tienda.pagos.client.dto;

import java.math.BigDecimal;

/**
 * Resumen del pedido obtenido desde el microservicio de pedidos.
 */
public class PedidoResumenDTO {

    private Long idPedido;
    private Long idUsuario;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String estado;

    public PedidoResumenDTO() {
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

package com.tienda.pedidos.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para cambiar el estado de un pedido.
 * Valores permitidos: PENDIENTE, PAGADO, RECHAZADO, CANCELADO, ENTREGADO.
 */
public class CambioEstadoDTO {

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

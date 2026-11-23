package com.tienda.pagos.client.dto;

/**
 * DTO enviado al microservicio de pedidos para cambiar su estado.
 */
public class CambioEstadoDTO {

    private String estado;

    public CambioEstadoDTO() {
    }

    public CambioEstadoDTO(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

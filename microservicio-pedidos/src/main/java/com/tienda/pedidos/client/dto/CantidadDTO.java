package com.tienda.pedidos.client.dto;

/**
 * Cuerpo de peticion con la cantidad de unidades para reservar o liberar stock.
 */
public class CantidadDTO {

    private Integer cantidad;

    public CantidadDTO() {
    }

    public CantidadDTO(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}

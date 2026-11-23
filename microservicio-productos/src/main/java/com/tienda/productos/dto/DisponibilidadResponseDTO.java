package com.tienda.productos.dto;

/**
 * Datos de salida con la disponibilidad de un producto.
 */
public class DisponibilidadResponseDTO {

    private Long idProducto;
    private Integer stock;
    private boolean disponible;

    public DisponibilidadResponseDTO() {
    }

    public DisponibilidadResponseDTO(Long idProducto, Integer stock, boolean disponible) {
        this.idProducto = idProducto;
        this.stock = stock;
        this.disponible = disponible;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}

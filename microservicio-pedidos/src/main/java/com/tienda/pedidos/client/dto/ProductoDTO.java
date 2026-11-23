package com.tienda.pedidos.client.dto;

import java.math.BigDecimal;

/**
 * Representacion del producto tal como lo expone el microservicio de productos.
 * El precio se usa para calcular los subtotales del pedido.
 */
public class ProductoDTO {

    private Long idProducto;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    private String categoria;
    private Boolean estado;

    public ProductoDTO() {
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}

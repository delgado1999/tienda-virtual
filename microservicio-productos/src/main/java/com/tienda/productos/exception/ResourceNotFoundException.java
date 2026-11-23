package com.tienda.productos.exception;

/**
 * Excepcion lanzada cuando no se encuentra un recurso solicitado.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}

package com.tienda.productos.exception;

/**
 * Excepcion para errores de reglas de negocio.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String mensaje) {
        super(mensaje);
    }
}

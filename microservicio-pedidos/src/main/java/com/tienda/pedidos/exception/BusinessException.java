package com.tienda.pedidos.exception;

/**
 * Excepcion para errores de reglas de negocio.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String mensaje) {
        super(mensaje);
    }
}

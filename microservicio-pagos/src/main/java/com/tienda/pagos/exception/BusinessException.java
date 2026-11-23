package com.tienda.pagos.exception;

/**
 * Excepcion para errores de reglas de negocio.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String mensaje) {
        super(mensaje);
    }
}

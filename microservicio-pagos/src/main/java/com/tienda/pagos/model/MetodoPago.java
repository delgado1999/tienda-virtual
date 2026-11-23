package com.tienda.pagos.model;

/**
 * Metodos de pago disponibles dentro del sistema.
 * Todos los pagos son simulados, no se integra ninguna pasarela real.
 */
public enum MetodoPago {
    EFECTIVO,
    YAPE,
    PLIN,
    TRANSFERENCIA,
    TARJETA_PRUEBA
}

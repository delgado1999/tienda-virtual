package com.tienda.pedidos.model;

/**
 * Estados posibles del ciclo de vida de un pedido.
 */
public enum PedidoEstado {
    PENDIENTE,
    PAGADO,
    RECHAZADO,
    CANCELADO,
    ENTREGADO
}

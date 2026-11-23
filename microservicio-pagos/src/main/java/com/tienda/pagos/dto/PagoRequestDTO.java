package com.tienda.pagos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos requeridos para simular el pago de un pedido.
 */
public class PagoRequestDTO {

    @NotNull(message = "El id del pedido es obligatorio")
    private Long idPedido;

    @NotBlank(message = "El metodo de pago es obligatorio")
    private String metodoPago;

    /** Resultado opcional: "APROBADO" o "RECHAZADO". Si no se envia, se decide segun las reglas. */
    private String simularResultado;

    /** Numero de tarjeta opcional, solo para el metodo TARJETA_PRUEBA. */
    private String numeroTarjeta;

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getSimularResultado() {
        return simularResultado;
    }

    public void setSimularResultado(String simularResultado) {
        this.simularResultado = simularResultado;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }
}

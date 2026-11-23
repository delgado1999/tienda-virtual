package com.tienda.pagos.client;

import com.tienda.pagos.client.dto.CambioEstadoDTO;
import com.tienda.pagos.client.dto.PedidoResumenDTO;
import com.tienda.pagos.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Cliente REST para comunicarse con el microservicio de pedidos.
 */
@Component
public class PedidoClient {

    private final WebClient pedidoWebClient;

    public PedidoClient(WebClient pedidoWebClient) {
        this.pedidoWebClient = pedidoWebClient;
    }

    /**
     * Obtiene el resumen de un pedido por su identificador.
     */
    public PedidoResumenDTO obtenerPedido(Long idPedido) {
        try {
            return pedidoWebClient.get()
                    .uri("/ventas/{idPedido}", idPedido)
                    .retrieve()
                    .bodyToMono(PedidoResumenDTO.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new BusinessException("El pedido con id " + idPedido + " no existe");
            }
            throw new BusinessException("Error al consultar el pedido " + idPedido
                    + ": " + ex.getStatusCode() + " " + ex.getStatusText());
        } catch (WebClientRequestException ex) {
            throw new BusinessException("No se pudo conectar con el microservicio de pedidos: " + ex.getMessage());
        }
    }

    /**
     * Actualiza el estado de un pedido en el microservicio de pedidos.
     */
    public void actualizarEstado(Long idPedido, String estado) {
        try {
            pedidoWebClient.patch()
                    .uri("/ventas/{idPedido}/estado", idPedido)
                    .bodyValue(new CambioEstadoDTO(estado))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException ex) {
            throw new BusinessException("Error al actualizar el estado del pedido " + idPedido
                    + ": " + ex.getStatusCode() + " " + ex.getStatusText());
        } catch (WebClientRequestException ex) {
            throw new BusinessException("No se pudo conectar con el microservicio de pedidos: " + ex.getMessage());
        }
    }
}

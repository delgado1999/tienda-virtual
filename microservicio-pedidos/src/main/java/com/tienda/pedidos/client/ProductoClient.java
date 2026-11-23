package com.tienda.pedidos.client;

import com.tienda.pedidos.client.dto.CantidadDTO;
import com.tienda.pedidos.client.dto.ProductoDTO;
import com.tienda.pedidos.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Cliente REST hacia el microservicio de productos.
 * Se utiliza para consultar productos, reservar stock al crear un pedido
 * y liberar stock cuando un pedido se cancela (o cuando falla una reserva).
 */
@Component
public class ProductoClient {

    private final WebClient productoWebClient;

    public ProductoClient(@Qualifier("productoWebClient") WebClient productoWebClient) {
        this.productoWebClient = productoWebClient;
    }

    /**
     * Consulta un producto por su id.
     */
    public ProductoDTO obtenerProducto(Long id) {
        try {
            return productoWebClient.get()
                    .uri("/productos/{id}", id)
                    .retrieve()
                    .bodyToMono(ProductoDTO.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new BusinessException("Producto no encontrado con id " + id);
            }
            throw new BusinessException("Error al consultar el producto con id " + id
                    + " (" + e.getStatusCode().value() + ")");
        } catch (WebClientRequestException e) {
            throw new BusinessException("No se pudo comunicar con el servicio de productos");
        }
    }

    /**
     * Reserva (descuenta) stock de un producto en el microservicio de productos.
     */
    public void reservarStock(Long id, Integer cantidad) {
        try {
            productoWebClient.post()
                    .uri("/productos/{id}/reservar", id)
                    .bodyValue(new CantidadDTO(cantidad))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new BusinessException("Producto no encontrado con id " + id);
            }
            throw new BusinessException("No se pudo reservar el stock del producto " + id
                    + " (" + e.getStatusCode().value() + ")");
        } catch (WebClientRequestException e) {
            throw new BusinessException("No se pudo comunicar con el servicio de productos");
        }
    }

    /**
     * Libera (devuelve) stock de un producto en el microservicio de productos.
     */
    public void liberarStock(Long id, Integer cantidad) {
        try {
            productoWebClient.post()
                    .uri("/productos/{id}/liberar", id)
                    .bodyValue(new CantidadDTO(cantidad))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new BusinessException("Producto no encontrado con id " + id);
            }
            throw new BusinessException("No se pudo liberar el stock del producto " + id
                    + " (" + e.getStatusCode().value() + ")");
        } catch (WebClientRequestException e) {
            throw new BusinessException("No se pudo comunicar con el servicio de productos");
        }
    }
}

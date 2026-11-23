package com.tienda.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

/**
 * Manejador global de errores del gateway. Devuelve respuestas JSON uniformes
 * (timestamp, status, error, mensaje, ruta) para cualquier error no controlado,
 * incluyendo el caso en que un microservicio destino no esta disponible (503).
 */
@Order(-1)
@Component
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // Si la respuesta ya empezo a enviarse, no se puede reescribir: se propaga el error.
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus estado;
        String error;
        String mensaje;

        if (esServicioNoDisponible(ex)) {
            estado = HttpStatus.SERVICE_UNAVAILABLE;
            error = "Servicio no disponible";
            mensaje = "Servicio no disponible: el microservicio destino no responde";
        } else if (ex instanceof ResponseStatusException rse) {
            estado = HttpStatus.resolve(rse.getStatusCode().value());
            if (estado == null) {
                estado = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            error = estado.getReasonPhrase();
            mensaje = rse.getReason() != null ? rse.getReason() : "Error al procesar la peticion";
        } else {
            estado = HttpStatus.INTERNAL_SERVER_ERROR;
            error = "Error interno";
            mensaje = ex.getMessage() != null ? ex.getMessage() : "Error interno al procesar la peticion";
        }

        response.setStatusCode(estado);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String ruta = exchange.getRequest().getPath().value();
        ErrorResponse cuerpo = new ErrorResponse(estado.value(), error, mensaje, ruta);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(cuerpo);
        } catch (JsonProcessingException e) {
            bytes = ("{\"status\":" + estado.value() + ",\"error\":\"" + error
                    + "\",\"mensaje\":\"" + mensaje + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Detecta si el error corresponde a un microservicio destino no disponible.
     */
    private boolean esServicioNoDisponible(Throwable ex) {
        Throwable causa = ex;
        while (causa != null) {
            if (causa instanceof NotFoundException || causa instanceof ConnectException) {
                return true;
            }
            causa = causa.getCause();
        }
        return ex instanceof ResponseStatusException rse
                && rse.getStatusCode().value() == HttpStatus.SERVICE_UNAVAILABLE.value();
    }
}

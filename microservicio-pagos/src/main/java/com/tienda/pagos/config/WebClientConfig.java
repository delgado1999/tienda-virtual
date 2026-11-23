package com.tienda.pagos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuracion del cliente WebClient usado para comunicarse con el microservicio de pedidos.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient pedidoWebClient(@Value("${pedidos.uri}") String pedidosUri) {
        return WebClient.builder()
                .baseUrl(pedidosUri)
                .build();
    }
}

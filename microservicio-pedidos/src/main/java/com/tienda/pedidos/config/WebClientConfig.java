package com.tienda.pedidos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuracion del cliente HTTP (WebClient) para comunicarse con el
 * microservicio de productos.
 */
@Configuration
public class WebClientConfig {

    private final String productosUri;

    public WebClientConfig(@Value("${productos.uri}") String productosUri) {
        this.productosUri = productosUri;
    }

    @Bean
    public WebClient productoWebClient() {
        return WebClient.builder()
                .baseUrl(productosUri)
                .build();
    }
}

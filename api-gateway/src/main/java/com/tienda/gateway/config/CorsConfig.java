package com.tienda.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * Documenta la estrategia CORS del API Gateway.
 *
 * El sistema usa UNA sola estrategia CORS: la configuracion global declarada en
 * {@code application.yml} bajo {@code spring.cloud.gateway.globalcors}. Esa estrategia
 * aplica a todas las rutas ({@code /**}), permite cualquier origen, metodo y cabecera,
 * y responde automaticamente a las peticiones preflight OPTIONS.
 *
 * Deliberadamente NO se registra un {@code CorsWebFilter} como bean: hacerlo duplicaria
 * la configuracion global y podria provocar respuestas con cabeceras CORS repetidas.
 * Si en el futuro se necesita un control mas fino, se debe migrar por completo a un bean
 * {@code CorsWebFilter} y retirar el bloque {@code globalcors} para seguir teniendo
 * una unica fuente de verdad.
 */
@Configuration
public class CorsConfig {
    // La configuracion efectiva de CORS vive en application.yml (globalcors).
    // Esta clase existe unicamente para centralizar y documentar dicha decision.
}

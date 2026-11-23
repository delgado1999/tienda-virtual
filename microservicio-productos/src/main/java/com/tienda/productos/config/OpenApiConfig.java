package com.tienda.productos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de Swagger / OpenAPI para el microservicio de productos.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservicio de Productos - Tienda Virtual")
                        .description("API para la gestion del catalogo de productos de la tienda virtual")
                        .version("1.0.0")
                        .contact(new Contact().name("Equipo Tienda Virtual")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

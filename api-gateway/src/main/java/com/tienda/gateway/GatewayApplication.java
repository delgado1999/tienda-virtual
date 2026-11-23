package com.tienda.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del API Gateway del sistema de tienda virtual.
 *
 * El gateway es el punto unico de entrada: recibe todas las peticiones del frontend,
 * aplica CORS, enruta hacia cada microservicio y centraliza la validacion del token JWT
 * y de los roles antes de reenviar la peticion.
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

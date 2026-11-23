package com.tienda.pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del microservicio de pagos.
 * Gestiona los pagos simulados de los pedidos de la tienda virtual.
 */
@SpringBootApplication
public class PagosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagosApplication.class, args);
    }
}

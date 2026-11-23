package com.tienda.pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del microservicio de pedidos.
 * Se encarga de la gestion de ventas y pedidos de la tienda virtual,
 * comunicandose con el microservicio de productos para validar y reservar stock.
 */
@SpringBootApplication
public class PedidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PedidosApplication.class, args);
    }
}

-- =====================================================================
--  BASE DE DATOS: db_pedidos
--  Microservicio: microservicio-pedidos
--  Nota: id_usuario e id_producto son referencias logicas a otros
--        microservicios (no son claves foraneas fisicas entre bases).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS db_pedidos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE db_pedidos;

DROP TABLE IF EXISTS detalle_pedido;
DROP TABLE IF EXISTS pedidos;

CREATE TABLE pedidos (
    id_pedido      BIGINT        NOT NULL AUTO_INCREMENT,
    id_usuario     BIGINT        NOT NULL,
    subtotal       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total          DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estado         VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    stock_devuelto TINYINT(1)    NOT NULL DEFAULT 0,
    fecha_pedido   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_pedidos PRIMARY KEY (id_pedido)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE detalle_pedido (
    id_detalle      BIGINT        NOT NULL AUTO_INCREMENT,
    id_pedido       BIGINT        NOT NULL,
    id_producto     BIGINT        NOT NULL,
    nombre_producto VARCHAR(150)  NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    cantidad        INT           NOT NULL,
    subtotal        DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_detalle_pedido PRIMARY KEY (id_detalle),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido)
        REFERENCES pedidos (id_pedido) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Permisos para el usuario de aplicacion creado por Docker (MYSQL_USER)
GRANT ALL PRIVILEGES ON db_pedidos.* TO 'tienda'@'%';
FLUSH PRIVILEGES;

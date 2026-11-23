-- =====================================================================
--  BASE DE DATOS: db_pagos
--  Microservicio: microservicio-pagos
--  Nota: id_pedido es una referencia logica al microservicio de pedidos.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS db_pagos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE db_pagos;

DROP TABLE IF EXISTS pagos;

CREATE TABLE pagos (
    id_pago          BIGINT        NOT NULL AUTO_INCREMENT,
    id_pedido        BIGINT        NOT NULL,
    monto            DECIMAL(10,2) NOT NULL,
    metodo_pago      VARCHAR(30)   NOT NULL,
    estado_pago      VARCHAR(20)   NOT NULL,
    codigo_operacion VARCHAR(40)   NOT NULL,
    fecha_pago       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_pagos PRIMARY KEY (id_pago),
    CONSTRAINT uk_pagos_codigo_operacion UNIQUE (codigo_operacion)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Permisos para el usuario de aplicacion creado por Docker (MYSQL_USER)
GRANT ALL PRIVILEGES ON db_pagos.* TO 'tienda'@'%';
FLUSH PRIVILEGES;

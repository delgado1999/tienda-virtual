-- =====================================================================
--  BASE DE DATOS: db_usuarios
--  Microservicio: microservicio-usuarios
-- =====================================================================

CREATE DATABASE IF NOT EXISTS db_usuarios
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE db_usuarios;

DROP TABLE IF EXISTS usuarios;

CREATE TABLE usuarios (
    id_usuario      BIGINT       NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(120) NOT NULL,
    correo          VARCHAR(150) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    direccion       VARCHAR(200) NULL,
    telefono        VARCHAR(20)  NULL,
    rol             VARCHAR(20)  NOT NULL DEFAULT 'CLIENTE',
    estado          TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_registro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuarios PRIMARY KEY (id_usuario),
    CONSTRAINT uk_usuarios_correo UNIQUE (correo)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- NOTA: los usuarios (administrador y cliente demo) se crean automaticamente
-- al arrancar el microservicio-usuarios, para garantizar que la contrasena
-- quede cifrada con BCrypt. Ver config/DataInitializer.java

-- Permisos para el usuario de aplicacion creado por Docker (MYSQL_USER)
GRANT ALL PRIVILEGES ON db_usuarios.* TO 'tienda'@'%';
FLUSH PRIVILEGES;

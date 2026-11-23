-- =====================================================================
--  BASE DE DATOS: db_productos
--  Microservicio: microservicio-productos
-- =====================================================================

CREATE DATABASE IF NOT EXISTS db_productos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE db_productos;

DROP TABLE IF EXISTS productos;

CREATE TABLE productos (
    id_producto     BIGINT        NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(150)  NOT NULL,
    descripcion     VARCHAR(500)  NULL,
    precio          DECIMAL(10,2) NOT NULL,
    stock           INT           NOT NULL DEFAULT 0,
    imagen          VARCHAR(500)  NULL,
    categoria       VARCHAR(80)   NOT NULL,
    estado          TINYINT(1)    NOT NULL DEFAULT 1,
    fecha_registro  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_productos PRIMARY KEY (id_producto)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ------------------------------- DATOS DE PRUEBA -------------------------------
INSERT INTO productos (nombre, descripcion, precio, stock, imagen, categoria, estado) VALUES
('Laptop Lenovo IdeaPad 3',        'Laptop 15.6 pulgadas, Intel Core i5, 8GB RAM, 512GB SSD',            2499.90, 15,  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=modern%20silver%20laptop%20on%20a%20clean%20white%20desk%20product%20photo&image_size=landscape_4_3', 'Tecnologia', 1),
('Smartphone Samsung Galaxy A54',  'Smartphone 6.4 pulgadas, 128GB, camara 50MP, 5G',                     1349.00, 25,  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=modern%20smartphone%20black%20on%20white%20background%20product%20photo&image_size=landscape_4_3', 'Tecnologia', 1),
('Audifonos Bluetooth JBL Tune',   'Audifonos inalambricos con cancelacion pasiva de ruido',               249.90,  60,  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=wireless%20bluetooth%20headphones%20product%20photo%20white%20background&image_size=landscape_4_3', 'Audio', 1),
('Teclado Mecanico Redragon',      'Teclado mecanico RGB switches red para gaming',                        189.90,  40,  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=rgb%20mechanical%20gaming%20keyboard%20product%20photo%20dark%20background&image_size=landscape_4_3', 'Accesorios', 1),
('Mouse Logitech G203',            'Mouse gamer con sensor de 8000 DPI y 6 botones programables',           99.90,  80,  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=gaming%20mouse%20product%20photo%20white%20background&image_size=landscape_4_3', 'Accesorios', 1),
('Monitor LG 24 pulgadas FHD',     'Monitor IPS 24 pulgadas Full HD 75Hz con HDMI',                        649.00,  20,  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=computer%20monitor%2024%20inch%20product%20photo%20white%20background&image_size=landscape_4_3', 'Tecnologia', 1),
('Camiseta Deportiva Dry-Fit',     'Camiseta transpirable de secado rapido para entrenamiento',             59.90, 120,  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=sport%20t-shirt%20product%20photo%20white%20background&image_size=landscape_4_3', 'Ropa', 1),
('Zapatillas Running Pro',         'Zapatillas ligeras con amortiguacion para correr',                     279.90,  35,  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=running%20shoes%20product%20photo%20white%20background&image_size=landscape_4_3', 'Ropa', 1),
('Cafetera Electrica Oster',       'Cafetera de goteo 12 tazas con filtro permanente',                     199.90,  30,  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=electric%20coffee%20maker%20product%20photo%20white%20background&image_size=landscape_4_3', 'Hogar', 1),
('Mochila Antirrobo USB',          'Mochila para laptop 15.6 con puerto USB y compartimentos',             139.90,  50,  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=antitheft%20laptop%20backpack%20product%20photo%20white%20background&image_size=landscape_4_3', 'Accesorios', 1),
('Tablet Lenovo Tab M10',          'Tablet 10.1 pulgadas, 64GB, WiFi, Android',                            899.00,  18,  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=android%20tablet%20product%20photo%20white%20background&image_size=landscape_4_3', 'Tecnologia', 1),
('Libro Clean Code',               'Libro de buenas practicas de programacion por Robert C. Martin',        129.00,  45,  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=programming%20book%20on%20desk%20product%20photo&image_size=landscape_4_3', 'Libros', 1);

-- Producto desactivado para probar la regla "los productos desactivados no pueden comprarse"
INSERT INTO productos (nombre, descripcion, precio, stock, imagen, categoria, estado) VALUES
('Parlante Bluetooth Antiguo',     'Producto descontinuado por el fabricante',                             149.90,  5,   NULL, 'Audio', 0);

-- Permisos para el usuario de aplicacion creado por Docker (MYSQL_USER)
GRANT ALL PRIVILEGES ON db_productos.* TO 'tienda'@'%';
FLUSH PRIVILEGES;

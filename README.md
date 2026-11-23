# Tienda Virtual — Sistema con Arquitectura de Microservicios

Sistema académico de **tienda virtual** construido con una arquitectura de **microservicios** en **Spring Boot 3**, con un **API Gateway** como punto único de entrada, **bases de datos independientes por servicio**, **seguridad JWT** y un **frontend en Bootstrap 5**. Todo el sistema se puede levantar de forma reproducible con **Docker Compose**.

> Proyecto con fines **académicos / educativos**. Los pagos son **simulados** y no existe integración con pasarelas bancarias reales.

---

## 1. Descripción

El sistema permite:

- Registrar usuarios y autenticarse mediante **JWT** (roles `ADMIN` y `CLIENTE`).
- Gestionar un **catálogo de productos** (alta, edición, stock y baja lógica).
- **Registrar pedidos/ventas** validando precios y stock en el backend (nunca se confía en los datos enviados por el navegador).
- **Simular pagos** con distintos métodos (YAPE, PLIN, EFECTIVO, TRANSFERENCIA, TARJETA_PRUEBA) y determinar la aprobación/rechazo.
- Aplicar **reglas de negocio**: no vender sin stock, devolver stock al cancelar, impedir comprar productos desactivados, entre otras.

Cinco módulos Java (un gateway + cuatro microservicios) más un frontend estático, coordinados a través de una red Docker.

---

## 2. Tecnologías utilizadas

| Tecnología | Uso en el proyecto |
|---|---|
| **Java 21** | Lenguaje de los microservicios (LTS, compilado con `release 21`) |
| **Spring Boot 3** | Framework base de los 5 módulos |
| **Spring Cloud Gateway** (Spring Cloud 2023.0.1) | API Gateway, enrutamiento y filtro de seguridad |
| **Spring Security** | Seguridad del microservicio de usuarios |
| **JWT (jjwt 0.12.5, HS256)** | Autenticación stateless con token Bearer |
| **BCrypt** | Cifrado de contraseñas |
| **Spring Data JPA / Hibernate** | Persistencia contra MySQL |
| **MySQL 8** | Una base de datos por microservicio |
| **WebClient** | Comunicación HTTP entre microservicios |
| **Docker / Docker Compose** | Empaquetado y orquestación de contenedores |
| **Bootstrap 5** | Interfaz del frontend |
| **Swagger / OpenAPI** | Documentación interactiva de cada servicio |
| **Postman** | Colección de pruebas manuales del API |
| **Maven Wrapper** | Descarga Maven 3.9.6 automáticamente (no requiere Maven instalado) |

---

## 3. Arquitectura

El sistema es una arquitectura de microservicios con un **API Gateway** central que recibe todo el tráfico del frontend y lo enruta a cada servicio. **Cada microservicio tiene su propia base de datos** (patrón *database-per-service*), por lo que no existen claves foráneas físicas entre bases: las referencias entre servicios (por ejemplo `id_usuario` o `id_producto`) son **lógicas**.

| Módulo | Puerto | Base de datos | Responsabilidad |
|---|---|---|---|
| `api-gateway` | 8080 | — | Punto único de entrada, enrutamiento (`StripPrefix=1`) y **validación JWT + roles** |
| `microservicio-usuarios` | 8081 | `db_usuarios` | Registro, autenticación (JWT) y gestión de usuarios (Spring Security + BCrypt) |
| `microservicio-productos` | 8082 | `db_productos` | Catálogo de productos y control de stock |
| `microservicio-pedidos` | 8083 | `db_pedidos` | Registro de pedidos/ventas, reserva y devolución de stock |
| `microservicio-pagos` | 8084 | `db_pagos` | Pagos simulados y actualización del estado del pedido |
| `frontend` | 8085 (nginx) | — | Interfaz web estática (Bootstrap 5) |
| `mysql` | 3306 | (4 esquemas) | Servidor MySQL 8 compartido (un esquema por servicio) |

> **Nota:** aunque físicamente hay una sola instancia de MySQL en desarrollo, se crean **cuatro esquemas independientes** (`db_usuarios`, `db_productos`, `db_pedidos`, `db_pagos`), respetando la separación lógica de datos por microservicio.

### Rutas del API Gateway

Todas las rutas aplican el filtro `StripPrefix=1` (se elimina el prefijo `/api` antes de reenviar al servicio). El servicio recibe la ruta **sin** el `/api`.

| Ruta del gateway | Microservicio destino | Ruta expuesta por el servicio |
|---|---|---|
| `/api/usuarios/**` | `microservicio-usuarios` (8081) | `/usuarios/**` |
| `/api/auth/**` | `microservicio-usuarios` (8081) | `/auth/login` |
| `/api/productos/**` | `microservicio-productos` (8082) | `/productos/**` |
| `/api/ventas/**` | `microservicio-pedidos` (8083) | `/ventas/**` |
| `/api/pagos/**` | `microservicio-pagos` (8084) | `/pagos/**` |

> El gateway **no** usa Eureka ni balanceo `lb://`: cada destino es una URI directa configurable por variables de entorno (`USUARIOS_URI`, `PRODUCTOS_URI`, `PEDIDOS_URI`, `PAGOS_URI`).

---

## 4. Estructura de carpetas

```
tienda-virtual/
├── api-gateway/              -> puerto 8080 (Spring Cloud Gateway, Spring Cloud 2023.0.1)
├── microservicio-usuarios/   -> puerto 8081  (BD db_usuarios)  Spring Security + JWT + BCrypt
├── microservicio-productos/  -> puerto 8082  (BD db_productos)
├── microservicio-pedidos/    -> puerto 8083  (BD db_pedidos)
├── microservicio-pagos/      -> puerto 8084  (BD db_pagos)
├── frontend/                 -> puerto 8085 en Docker (nginx) / abrir HTML local
├── database/                 -> usuarios.sql, productos.sql, pedidos.sql, pagos.sql
├── postman/                  -> tienda-virtual-collection.json
├── docker-compose.yml
├── .env.example
├── mvnw.cmd / mvnw           -> Maven Wrapper (descarga Maven 3.9.6 automaticamente)
├── README.md
└── DOCUMENTACION.md
```

---

## 5. Requisitos previos

- **Java 21** (JDK 21; el proyecto está configurado con `<java.version>21</java.version>` en los 5 módulos).
- **Docker Desktop** con Docker Compose (para el modo contenedores).
- **MySQL 8** (solo para el modo sin Docker).
- **Maven**: **no** es necesario instalarlo; se incluye el **Maven Wrapper** (`mvnw.cmd` en Windows) que descarga Maven 3.9.6 automáticamente.
- **Postman** (opcional, para ejecutar la colección de pruebas).

---

## 6. Instalación y ejecución paso a paso (Windows 10/11)

### Opción A — Con Docker (recomendado)

1. Abrir **PowerShell** y situarse en la carpeta del proyecto:
   ```powershell
   cd tienda-virtual
   ```
2. Copiar el archivo de variables de entorno de ejemplo:
   ```powershell
   copy .env.example .env
   ```
   (Opcional) Editar `.env` para cambiar contraseñas, puertos o el secreto JWT.
3. Construir y levantar todo el sistema:
   ```powershell
   docker compose up --build
   ```
   La primera vez, Docker descarga las imágenes, compila los módulos y **ejecuta los scripts SQL** de `database/` al crear el volumen de MySQL.
4. En otra terminal, verificar que los contenedores estén arriba:
   ```powershell
   docker compose ps
   ```
   Deben aparecer: `tienda-mysql`, `microservicio-usuarios`, `microservicio-productos`, `microservicio-pedidos`, `microservicio-pagos`, `api-gateway` y `tienda-frontend`.
5. Acceder a:
   - **Frontend:** http://localhost:8085
   - **API Gateway:** http://localhost:8080
   - **Health del gateway:** http://localhost:8080/actuator/health

### Opción B — Sin Docker (local, con MySQL 8)

1. Tener **MySQL 8** en ejecución en `localhost:3306`.
2. Crear las bases de datos ejecutando los scripts de `database/` (en este orden):
   ```powershell
   mysql -u root -p < database/usuarios.sql
   mysql -u root -p < database/productos.sql
   mysql -u root -p < database/pedidos.sql
   mysql -u root -p < database/pagos.sql
   ```
3. Arrancar cada servicio en terminales separadas (el orden recomendado es: usuarios, productos, pedidos, pagos y por último el gateway):
   ```powershell
   mvnw.cmd -pl microservicio-usuarios spring-boot:run
   mvnw.cmd -pl microservicio-productos spring-boot:run
   mvnw.cmd -pl microservicio-pedidos spring-boot:run
   mvnw.cmd -pl microservicio-pagos spring-boot:run
   mvnw.cmd -pl api-gateway spring-boot:run
   ```
   > Ajustar usuario/contraseña de MySQL y el secreto JWT si es necesario mediante variables de entorno o en cada `application.yml`.
4. Abrir el frontend con doble clic en `frontend/index.html` o sirviéndolo con un servidor estático.
5. **Swagger** de cada servicio (acceso directo a su puerto):
   - http://localhost:8081/swagger-ui.html (usuarios)
   - http://localhost:8082/swagger-ui.html (productos)
   - http://localhost:8083/swagger-ui.html (pedidos)
   - http://localhost:8084/swagger-ui.html (pagos)

### Compilar sin ejecutar

```powershell
mvnw.cmd clean package -DskipTests
```

---

## 7. Credenciales de prueba

Se crean **automáticamente** al arrancar el microservicio de usuarios (`DataInitializer`):

| Rol | Correo | Contraseña |
|---|---|---|
| **ADMIN** | `admin@tienda.com` | `Admin123` |
| **CLIENTE** | `cliente@tienda.com` | `Cliente123` |

---

## 8. Puertos utilizados

| Servicio | Puerto | Descripción |
|---|---|---|
| API Gateway | 8080 | Punto único de entrada |
| Microservicio Usuarios | 8081 | Registro/login/usuarios + Swagger |
| Microservicio Productos | 8082 | Catálogo + Swagger |
| Microservicio Pedidos | 8083 | Ventas + Swagger |
| Microservicio Pagos | 8084 | Pagos + Swagger |
| Frontend | 8085 | Interfaz web (nginx en Docker) |
| MySQL | 3306 | Base de datos |

---

## 9. Endpoints principales

Todas las peticiones se realizan contra el **gateway** en `http://localhost:8080/api`.

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/usuarios` | Público | Registrar usuario |
| POST | `/api/auth/login` | Público | Login y obtención del token JWT |
| GET | `/api/usuarios` | ADMIN | Listar usuarios |
| GET | `/api/usuarios/{id}` | ADMIN | Consultar usuario |
| PUT | `/api/usuarios/{id}` | ADMIN | Actualizar usuario |
| PATCH | `/api/usuarios/{id}/estado` | ADMIN | Activar/desactivar usuario |
| POST | `/api/productos` | ADMIN | Crear producto |
| GET | `/api/productos` | Público | Listar productos |
| GET | `/api/productos/{id}` | Público | Consultar producto |
| GET | `/api/productos/categorias` | Público | Listar categorías |
| PUT | `/api/productos/{id}` | ADMIN | Actualizar producto |
| PATCH | `/api/productos/{id}/stock` | ADMIN | Actualizar stock |
| DELETE | `/api/productos/{id}` | ADMIN | Desactivar producto (baja lógica) |
| POST | `/api/ventas` | Autenticado | Crear pedido |
| GET | `/api/ventas` | ADMIN | Listar todos los pedidos |
| GET | `/api/ventas/{id}` | Autenticado | Consultar pedido |
| GET | `/api/ventas/usuario/{idUsuario}` | Autenticado | Pedidos de un usuario |
| PATCH | `/api/ventas/{id}/estado` | ADMIN | Cambiar estado del pedido |
| POST | `/api/pagos/simular` | Autenticado | Simular pago |
| GET | `/api/pagos/{id}` | ADMIN | Consultar pago |
| GET | `/api/pagos/pedido/{idPedido}` | ADMIN | Pagos de un pedido |

> **Acceso:** *Público* = sin token; *Autenticado* = token válido (cualquier rol); *ADMIN* = token con `rol=ADMIN`. Sin token → **401**; con rol insuficiente → **403**.

---

## 10. Ejecución de la colección Postman

1. Abrir Postman → **Import** → seleccionar `postman/tienda-virtual-collection.json`.
2. La colección ya define las variables `baseUrl` (`http://localhost:8080/api`) y `token` (vacío), y autenticación Bearer a nivel de colección.
3. **Ejecutar primero** la request **Usuarios → Login (guardar token)**: su script de test guarda el JWT en `{{token}}` para el resto de peticiones.
   - Para probar operaciones de **ADMIN**, hacer login con `admin@tienda.com` / `Admin123`.
   - Para probar operaciones de **CLIENTE**, hacer login con `cliente@tienda.com` / `Cliente123`.
4. Ejecutar el resto de requests (Productos, Pedidos, Pagos) en el orden que se prefiera.

---

## 11. Detener el sistema y consultar logs

```powershell
# Detener los contenedores (conserva los datos)
docker compose down

# Detener y BORRAR los datos persistidos (volumen de MySQL)
docker compose down -v

# Ver logs de un servicio concreto en vivo
docker compose logs -f microservicio-pedidos

# Ver logs de todos los servicios
docker compose logs -f
```

---

## 12. Limitaciones conocidas y notas

1. **Versión de Java:** el proyecto está configurado y compilado con **Java 21** (`mvnw clean package -DskipTests` → **BUILD SUCCESS** en los 6 módulos del reactor). El `pom.xml` de cada módulo usa `<java.version>21</java.version>` y los `Dockerfile` compilan con `maven:3.9.6-eclipse-temurin-21` y ejecutan con `eclipse-temurin:21-jre-alpine`.
2. **Docker no estaba instalado** en el entorno de desarrollo, por lo que `docker compose up --build` **no pudo ejecutarse ni verificarse**. Los `Dockerfile` y el `docker-compose.yml` se entregan listos, pero **pendientes de validación** en un equipo con Docker Desktop.
3. Los scripts SQL de `database/` se ejecutan **automáticamente la primera vez** que Docker crea el volumen de MySQL (montaje en `/docker-entrypoint-initdb.d`). Si el volumen ya existe, no se vuelven a ejecutar (usar `docker compose down -v` para reiniciar los datos).
4. Los microservicios de **productos, pedidos y pagos confían en las cabeceras `X-User-Id` y `X-User-Rol`** que inyecta el gateway. Las validaciones de rol se **centralizan en el API Gateway** y en el microservicio de usuarios (que sí implementa Spring Security). Se recomienda **no exponer esos servicios directamente a Internet**. Evolución recomendada: OAuth2/mTLS con tokens de servicio.
5. Los pagos son **simulados**; no hay pasarelas bancarias reales.
6. **No se ejecutaron pruebas de integración end-to-end con MySQL real**; las validaciones realizadas se limitan a la **compilación del código**. La colección Postman se entrega lista para ejecutar, pero sus resultados **no han sido verificados**.

---

## 13. Autoría y contexto académico

Proyecto académico desarrollado como taller de **arquitectura de microservicios**. Su objetivo es demostrar de forma práctica: separación de responsabilidades por servicio, base de datos por servicio, seguridad centralizada con JWT en un API Gateway, comunicación entre servicios con WebClient, consistencia de datos mediante reserva/devolución de stock y un despliegue reproducible con Docker Compose.

> Documentación técnica ampliada (diagramas, reglas de negocio y casos de prueba) en [`DOCUMENTACION.md`](DOCUMENTACION.md).

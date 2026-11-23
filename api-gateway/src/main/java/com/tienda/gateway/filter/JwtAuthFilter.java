package com.tienda.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tienda.gateway.exception.ErrorResponse;
import com.tienda.gateway.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Filtro global de autenticacion y autorizacion del API Gateway.
 *
 * El gateway es el punto unico de entrada del sistema, por lo que TODAS las
 * validaciones de token y de rol se centralizan aqui: los microservicios reciben
 * las cabeceras de identidad ya resueltas ({@code X-User-Id} y {@code X-User-Rol})
 * y no necesitan validar el JWT por su cuenta.
 *
 * Reglas aplicadas, en este orden:
 * <ol>
 *   <li>Las peticiones {@code OPTIONS} (preflight CORS) pasan sin validacion.</li>
 *   <li>Rutas publicas: no requieren token.</li>
 *   <li>Rutas solo ADMIN: requieren token valido con claim {@code rol} = "ADMIN".</li>
 *   <li>Cualquier otra ruta: requiere token valido (cualquier rol).</li>
 * </ol>
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public JwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String ruta = request.getPath().value();
        HttpMethod metodo = request.getMethod();

        // 1) Preflight CORS: nunca se valida el token.
        if (HttpMethod.OPTIONS.equals(metodo)) {
            return chain.filter(exchange);
        }

        // 2) Rutas publicas: se reenvian tal cual (sin cabeceras de identidad).
        if (esRutaPublica(metodo, ruta)) {
            return chain.filter(exchange);
        }

        // 3) A partir de aqui se exige un token JWT valido.
        String cabecera = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith("Bearer ")) {
            return responderError(exchange, HttpStatus.UNAUTHORIZED, "No autorizado",
                    "Debe enviar un token JWT valido en la cabecera Authorization (Bearer <token>)");
        }

        String token = cabecera.substring(7).trim();
        Claims claims;
        try {
            claims = jwtService.validarYObtenerClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            return responderError(exchange, HttpStatus.UNAUTHORIZED, "No autorizado",
                    "El token es invalido o ha expirado");
        }

        String rol = claims.get("rol", String.class);

        // 4) Rutas exclusivas de ADMIN.
        if (esRutaSoloAdmin(metodo, ruta) && !"ADMIN".equalsIgnoreCase(rol)) {
            return responderError(exchange, HttpStatus.FORBIDDEN, "Acceso denegado",
                    "No tiene permisos de administrador para realizar esta operacion");
        }

        // 5) Token valido: se propaga la identidad mediante cabeceras propias.
        Object idUsuario = claims.get("idUsuario");
        ServerHttpRequest peticionMutada = request.mutate()
                .header("X-User-Id", idUsuario == null ? "" : String.valueOf(idUsuario))
                .header("X-User-Rol", rol == null ? "" : rol)
                .build();

        return chain.filter(exchange.mutate().request(peticionMutada).build());
    }

    /**
     * Rutas que no requieren token: autenticacion, registro de usuarios y catalogo de productos.
     */
    private boolean esRutaPublica(HttpMethod metodo, String ruta) {
        if (matcher.match("/api/auth/**", ruta)) {
            return true;
        }
        if (matcher.match("/actuator/**", ruta)) {
            return true;
        }
        if (HttpMethod.GET.equals(metodo) && matcher.match("/api/productos/**", ruta)) {
            return true;
        }
        return HttpMethod.POST.equals(metodo) && matcher.match("/api/usuarios", ruta);
    }

    /**
     * Rutas que requieren el rol ADMIN.
     */
    private boolean esRutaSoloAdmin(HttpMethod metodo, String ruta) {
        // Gestion del catalogo (escritura).
        boolean escrituraProductos = HttpMethod.POST.equals(metodo)
                || HttpMethod.PUT.equals(metodo)
                || HttpMethod.PATCH.equals(metodo)
                || HttpMethod.DELETE.equals(metodo);
        if (escrituraProductos && matcher.match("/api/productos/**", ruta)) {
            return true;
        }

        // Gestion de usuarios (consulta y mantenimiento).
        boolean gestionUsuarios = HttpMethod.GET.equals(metodo)
                || HttpMethod.PUT.equals(metodo)
                || HttpMethod.PATCH.equals(metodo)
                || HttpMethod.DELETE.equals(metodo);
        if (gestionUsuarios && matcher.match("/api/usuarios/**", ruta)) {
            return true;
        }

        // Listado completo de ventas (no aplica a /api/ventas/usuario/** ni a /api/ventas/{id}).
        if (HttpMethod.GET.equals(metodo) && esListadoVentas(ruta)) {
            return true;
        }

        // Actualizacion administrativa de ventas.
        if (HttpMethod.PATCH.equals(metodo) && matcher.match("/api/ventas/**", ruta)) {
            return true;
        }

        // Consulta de pagos.
        return HttpMethod.GET.equals(metodo) && matcher.match("/api/pagos/**", ruta);
    }

    /**
     * El listado completo de ventas es la ruta exacta {@code /api/ventas}, no sus subrutas.
     */
    private boolean esListadoVentas(String ruta) {
        return matcher.match("/api/ventas", ruta) || matcher.match("/api/ventas/", ruta);
    }

    /**
     * Escribe una respuesta JSON uniforme y corta la cadena de filtros.
     */
    private Mono<Void> responderError(ServerWebExchange exchange, HttpStatus estado, String error, String mensaje) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(estado);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String ruta = exchange.getRequest().getPath().value();
        ErrorResponse cuerpo = new ErrorResponse(estado.value(), error, mensaje, ruta);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(cuerpo);
        } catch (JsonProcessingException e) {
            bytes = ("{\"status\":" + estado.value() + ",\"error\":\"" + error
                    + "\",\"mensaje\":\"" + mensaje + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // Alta prioridad: la seguridad se evalua antes que el enrutamiento del gateway.
        return -1;
    }
}

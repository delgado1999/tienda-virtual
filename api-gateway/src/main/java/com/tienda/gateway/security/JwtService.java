package com.tienda.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Servicio reactivo (sin Spring Security) encargado de validar los tokens JWT
 * emitidos por el microservicio de usuarios. Comparte la misma clave secreta
 * ({@code jwt.secret}) para poder verificar la firma y la expiracion.
 */
@Service
public class JwtService {

    private final SecretKey clave;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Valida el token (firma correcta y no expirado) y devuelve sus claims.
     *
     * @throws JwtException            si el token es invalido o esta expirado.
     * @throws IllegalArgumentException si el token es nulo o esta vacio.
     */
    public Claims validarYObtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Indica si el token es valido (firma correcta y no expirado).
     */
    public boolean esValido(String token) {
        try {
            validarYObtenerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

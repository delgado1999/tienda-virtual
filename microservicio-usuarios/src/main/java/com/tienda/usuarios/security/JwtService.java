package com.tienda.usuarios.security;

import com.tienda.usuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Servicio encargado de generar y validar los tokens JWT.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracion;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expiracion) {
        this.clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracion = expiracion;
    }

    /**
     * Genera un token JWT firmado con los datos del usuario.
     */
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date vencimiento = new Date(ahora.getTime() + expiracion);
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("idUsuario", usuario.getIdUsuario())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(vencimiento)
                .signWith(clave)
                .compact();
    }

    /**
     * Extrae el conjunto de claims del token.
     */
    public Claims obtenerClaims(String token) {
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
            obtenerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpiracion() {
        return expiracion;
    }
}

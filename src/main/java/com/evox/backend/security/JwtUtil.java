package com.evox.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secreto;

    @Value("${app.jwt.expiracion-ms}")
    private long expiracionMs;

    private SecretKey obtenerClave() {
        return Keys.hmacShaKeyFor(secreto.getBytes());
    }

    /** Genera un token para el correo y rol indicados. */
    public String generarToken(String correo, String rol) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .setSubject(correo)
                .claim("rol", rol)
                .setIssuedAt(ahora)
                .setExpiration(expira)
                .signWith(obtenerClave(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Extrae y valida las claims del token */
    public Claims validarYObtenerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(obtenerClave())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String obtenerCorreo(String token) {
        return validarYObtenerClaims(token).getSubject();
    }

    public String obtenerRol(String token) {
        return validarYObtenerClaims(token).get("rol", String.class);
    }
}

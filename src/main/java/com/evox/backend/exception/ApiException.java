package com.evox.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepcion generica para errores de negocio (correo duplicado, stock
 * insuficiente, producto no encontrado, etc). Cada servicio decide con
 * que codigo HTTP debe responder el controlador.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

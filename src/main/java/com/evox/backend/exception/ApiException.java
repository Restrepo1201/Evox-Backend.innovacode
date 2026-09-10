package com.evox.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepcion generica para errores de negocio
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

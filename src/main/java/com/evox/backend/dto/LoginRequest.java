package com.evox.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Datos que llegan en POST /api/v1/auth/login */
@Data
public class LoginRequest {
    @NotBlank
    private String correo;

    @NotBlank
    private String password;
}

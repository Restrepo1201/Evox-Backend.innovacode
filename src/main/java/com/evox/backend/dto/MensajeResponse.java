package com.evox.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Respuesta simple de tipo {"mensaje": "..."} usada en varios endpoints. */
@Data
@AllArgsConstructor
public class MensajeResponse {
    private String mensaje;
}

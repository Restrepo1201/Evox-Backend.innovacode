package com.evox.backend.dto;

import com.evox.backend.model.Rol;
import com.evox.backend.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private Rol rol;

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getApellido(), u.getCorreo(), u.getRol());
    }
}

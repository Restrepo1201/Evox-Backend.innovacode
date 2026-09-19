package com.evox.backend.service;

import com.evox.backend.dto.LoginRequest;
import com.evox.backend.dto.LoginResponse;
import com.evox.backend.dto.RegisterRequest;
import com.evox.backend.dto.UsuarioAdminResponse;
import com.evox.backend.exception.ApiException;
import com.evox.backend.model.Perfil;
import com.evox.backend.model.Rol;
import com.evox.backend.repository.PerfilRepository;
import com.evox.backend.security.JwtUtil;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public PerfilService(PerfilRepository perfilRepository,
                         PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /** Registra un nuevo cliente. El primer admin se crea manualmente en la BD. */
    public Perfil registrar(RegisterRequest datos) {
        if (perfilRepository.existsByCorreo(datos.getCorreo())) {
            throw new ApiException(HttpStatus.CONFLICT, "El correo ya esta registrado");
        }

        Perfil perfil = new Perfil();
        perfil.setCorreo(datos.getCorreo());
        perfil.setNombreCompleto((datos.getNombre() + " " + datos.getApellido()).trim());
        perfil.setPassword(passwordEncoder.encode(datos.getPassword()));
        perfil.setRol(Rol.CLIENTE);
        return perfilRepository.save(perfil);
    }

    /** Valida las credenciales y devuelve un token JWT si son correctas. */
    public LoginResponse iniciarSesion(LoginRequest datos) {
        Perfil perfil = perfilRepository.findByCorreo(datos.getCorreo())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

        if (!passwordEncoder.matches(datos.getPassword(), perfil.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        String token = jwtUtil.generarToken(perfil.getCorreo(), perfil.getRol().name());
        var usuarioBasico = new LoginResponse.UsuarioBasico(perfil.getId(), perfil.getNombreCompleto(), perfil.getRol().name());
        return new LoginResponse(token, usuarioBasico);
    }

    /** Busca el perfil autenticado a partir de su correo (guardado en el token). */
    public Perfil obtenerPorCorreo(String correo) {
        return perfilRepository.findByCorreo(correo)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    /** GET /api/v1/perfiles : lista todos los usuarios (solo ADMINISTRADOR). */
    public List<UsuarioAdminResponse> listarUsuarios() {
        return perfilRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion")).stream()
                .map(UsuarioAdminResponse::desde)
                .toList();
    }
}
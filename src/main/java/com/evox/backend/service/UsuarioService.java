package com.evox.backend.service;

import com.evox.backend.dto.LoginRequest;
import com.evox.backend.dto.LoginResponse;
import com.evox.backend.dto.RegisterRequest;
import com.evox.backend.exception.ApiException;
import com.evox.backend.model.Carrito;
import com.evox.backend.model.Rol;
import com.evox.backend.model.Usuario;
import com.evox.backend.repository.CarritoRepository;
import com.evox.backend.repository.UsuarioRepository;
import com.evox.backend.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UsuarioService(UsuarioRepository usuarioRepository, CarritoRepository carritoRepository,
                           PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.carritoRepository = carritoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /** Registra un nuevo cliente. El primer usuario del sistema podria ser un ADMIN manualmente en la BD. */
    public Usuario registrar(RegisterRequest datos) {
        if (usuarioRepository.existsByCorreo(datos.getCorreo())) {
            throw new ApiException(HttpStatus.CONFLICT, "El correo ya esta registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());
        usuario.setCorreo(datos.getCorreo());
        usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        usuario.setRol(Rol.CLIENTE);
        usuario = usuarioRepository.save(usuario);

        // Cada cliente nuevo recibe un carrito vacio propio.
        Carrito carrito = new Carrito();
        carrito.setUsuario(usuario);
        carritoRepository.save(carrito);

        return usuario;
    }

    /** Valida las credenciales y devuelve un token JWT si son correctas. */
    public LoginResponse iniciarSesion(LoginRequest datos) {
        Usuario usuario = usuarioRepository.findByCorreo(datos.getCorreo())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

        if (!passwordEncoder.matches(datos.getPassword(), usuario.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        String token = jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol().name());
        var usuarioBasico = new LoginResponse.UsuarioBasico(usuario.getId(), usuario.getNombre(), usuario.getRol().name());
        return new LoginResponse(token, usuarioBasico);
    }

    /** Busca el usuario actualmente autenticado a partir de su correo (guardado en el token). */
    public Usuario obtenerPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }
}

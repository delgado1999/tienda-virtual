package com.tienda.usuarios.service;

import com.tienda.usuarios.dto.LoginRequestDTO;
import com.tienda.usuarios.dto.LoginResponseDTO;
import com.tienda.usuarios.exception.BusinessException;
import com.tienda.usuarios.model.Usuario;
import com.tienda.usuarios.repository.UsuarioRepository;
import com.tienda.usuarios.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticacion: valida credenciales y emite el token JWT.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        String correo = dto.getCorreo().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new BusinessException("Credenciales invalidas"));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            throw new BusinessException("Credenciales invalidas");
        }

        if (Boolean.FALSE.equals(usuario.getEstado())) {
            throw new BusinessException("El usuario se encuentra desactivado. Contacte al administrador");
        }

        String token = jwtService.generarToken(usuario);
        return new LoginResponseDTO(token, "Bearer", usuario.getIdUsuario(),
                usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
    }
}

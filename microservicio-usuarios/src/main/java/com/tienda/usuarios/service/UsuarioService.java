package com.tienda.usuarios.service;

import com.tienda.usuarios.dto.UsuarioRequestDTO;
import com.tienda.usuarios.dto.UsuarioResponseDTO;
import com.tienda.usuarios.dto.UsuarioUpdateDTO;
import com.tienda.usuarios.exception.BusinessException;
import com.tienda.usuarios.exception.ResourceNotFoundException;
import com.tienda.usuarios.model.Rol;
import com.tienda.usuarios.model.Usuario;
import com.tienda.usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reglas de negocio para la gestion de usuarios.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario validando que el correo no este repetido
     * y cifrando la contrasena con BCrypt.
     */
    @Transactional
    public UsuarioResponseDTO registrar(UsuarioRequestDTO dto) {
        String correo = dto.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new BusinessException("El correo " + correo + " ya se encuentra registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setDireccion(dto.getDireccion());
        usuario.setTelefono(dto.getTelefono());
        usuario.setRol(resolverRol(dto.getRol(), Rol.CLIENTE));
        usuario.setEstado(Boolean.TRUE);

        Usuario guardado = usuarioRepository.save(usuario);
        return mapear(guardado);
    }

    /**
     * Lista todos los usuarios registrados.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream().map(this::mapear).toList();
    }

    /**
     * Obtiene un usuario por su identificador.
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        return mapear(buscarOFallar(id));
    }

    /**
     * Actualiza los datos personales de un usuario.
     */
    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = buscarOFallar(id);
        String correo = dto.getCorreo().trim().toLowerCase();

        usuarioRepository.findByCorreo(correo).ifPresent(existente -> {
            if (!existente.getIdUsuario().equals(id)) {
                throw new BusinessException("El correo " + correo + " ya pertenece a otro usuario");
            }
        });

        usuario.setNombre(dto.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setDireccion(dto.getDireccion());
        usuario.setTelefono(dto.getTelefono());

        return mapear(usuarioRepository.save(usuario));
    }

    /**
     * Activa o desactiva un usuario.
     */
    @Transactional
    public UsuarioResponseDTO cambiarEstado(Long id, Boolean estado) {
        Usuario usuario = buscarOFallar(id);
        if (Boolean.FALSE.equals(estado) && usuario.getRol() == Rol.ADMIN) {
            long adminsActivos = usuarioRepository.findAll().stream()
                    .filter(u -> u.getRol() == Rol.ADMIN && Boolean.TRUE.equals(u.getEstado()))
                    .count();
            if (adminsActivos <= 1) {
                throw new BusinessException("No se puede desactivar al unico administrador activo del sistema");
            }
        }
        usuario.setEstado(estado);
        return mapear(usuarioRepository.save(usuario));
    }

    /**
     * Verifica si un usuario existe y esta activo. Usado por otros servicios.
     */
    @Transactional(readOnly = true)
    public boolean existeYEstaActivo(Long id) {
        return usuarioRepository.findById(id)
                .map(u -> Boolean.TRUE.equals(u.getEstado()))
                .orElse(false);
    }

    // ----------------------------- Metodos de apoyo -----------------------------

    public Usuario buscarOFallar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));
    }

    private Rol resolverRol(String rol, Rol porDefecto) {
        if (rol == null || rol.isBlank()) {
            return porDefecto;
        }
        try {
            return Rol.valueOf(rol.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Rol invalido: " + rol + ". Valores permitidos: ADMIN, CLIENTE");
        }
    }

    private UsuarioResponseDTO mapear(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getDireccion(),
                usuario.getTelefono(),
                usuario.getRol(),
                usuario.getEstado(),
                usuario.getFechaRegistro());
    }
}

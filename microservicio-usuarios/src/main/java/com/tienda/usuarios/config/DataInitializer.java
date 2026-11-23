package com.tienda.usuarios.config;

import com.tienda.usuarios.model.Rol;
import com.tienda.usuarios.model.Usuario;
import com.tienda.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea los usuarios iniciales (administrador y cliente demo) al arrancar
 * el microservicio, garantizando que la contrasena quede cifrada con BCrypt.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.nombre}")
    private String adminNombre;

    @Value("${app.admin.correo}")
    private String adminCorreo;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste(adminNombre, adminCorreo, adminPassword, Rol.ADMIN);
        crearSiNoExiste("Cliente Demo", "cliente@tienda.com", "Cliente123", Rol.CLIENTE);
    }

    private void crearSiNoExiste(String nombre, String correo, String password, Rol rol) {
        String correoLimpio = correo.trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correoLimpio)) {
            log.info("Usuario {} ya existe, no se crea nuevamente", correoLimpio);
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correoLimpio);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setEstado(Boolean.TRUE);
        usuario.setDireccion("Av. Principal 123");
        usuario.setTelefono("999999999");
        usuarioRepository.save(usuario);
        log.info("Usuario {} creado con rol {}", correoLimpio, rol);
    }
}

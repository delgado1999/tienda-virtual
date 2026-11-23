package com.tienda.usuarios.dto;

import com.tienda.usuarios.model.Rol;

/**
 * Respuesta del inicio de sesion: incluye el token JWT y datos basicos del usuario.
 */
public class LoginResponseDTO {

    private String token;
    private String tipo;
    private Long idUsuario;
    private String nombre;
    private String correo;
    private Rol rol;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, String tipo, Long idUsuario, String nombre, String correo, Rol rol) {
        this.token = token;
        this.tipo = tipo;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}

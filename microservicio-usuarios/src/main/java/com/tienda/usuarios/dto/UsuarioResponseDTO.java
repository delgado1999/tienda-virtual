package com.tienda.usuarios.dto;

import com.tienda.usuarios.model.Rol;

import java.time.LocalDateTime;

/**
 * Datos de salida de un usuario. No expone la contrasena.
 */
public class UsuarioResponseDTO {

    private Long idUsuario;
    private String nombre;
    private String correo;
    private String direccion;
    private String telefono;
    private Rol rol;
    private Boolean estado;
    private LocalDateTime fechaRegistro;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Long idUsuario, String nombre, String correo, String direccion,
                              String telefono, Rol rol, Boolean estado, LocalDateTime fechaRegistro) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.direccion = direccion;
        this.telefono = telefono;
        this.rol = rol;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}

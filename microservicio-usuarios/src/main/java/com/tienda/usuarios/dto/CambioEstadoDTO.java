package com.tienda.usuarios.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para activar o desactivar un usuario.
 */
public class CambioEstadoDTO {

    @NotNull(message = "El estado es obligatorio (true = activo, false = inactivo)")
    private Boolean estado;

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}

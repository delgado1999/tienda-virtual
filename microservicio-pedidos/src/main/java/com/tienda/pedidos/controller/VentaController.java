package com.tienda.pedidos.controller;

import com.tienda.pedidos.dto.CambioEstadoDTO;
import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de gestion de ventas y pedidos.
 */
@RestController
@RequestMapping("/ventas")
@Tag(name = "Ventas", description = "Gestion de ventas y pedidos de la tienda virtual")
public class VentaController {

    private final PedidoService pedidoService;

    public VentaController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo pedido", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PedidoResponseDTO> crear(@Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(dto));
    }

    @GetMapping
    @Operation(summary = "Listar todos los pedidos", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PedidoResponseDTO>> listar() {
        return ResponseEntity.ok(pedidoService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar pedido por ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PedidoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Listar pedidos de un usuario", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PedidoResponseDTO>> listarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(pedidoService.listarPorUsuario(idUsuario));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de un pedido", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoDTO dto) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, dto.getEstado()));
    }
}

package com.tienda.pagos.controller;

import com.tienda.pagos.dto.PagoRequestDTO;
import com.tienda.pagos.dto.PagoResponseDTO;
import com.tienda.pagos.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de gestion de pagos simulados.
 */
@RestController
@RequestMapping("/pagos")
@Tag(name = "Pagos", description = "Simulacion de pagos de los pedidos de la tienda virtual")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping("/simular")
    @Operation(summary = "Simular el pago de un pedido")
    public ResponseEntity<PagoResponseDTO> simular(@Valid @RequestBody PagoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.simularPago(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un pago por ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PagoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    @GetMapping("/pedido/{idPedido}")
    @Operation(summary = "Listar los pagos de un pedido", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PagoResponseDTO>> listarPorPedido(@PathVariable Long idPedido) {
        return ResponseEntity.ok(pagoService.listarPorPedido(idPedido));
    }
}

package com.tienda.productos.controller;

import com.tienda.productos.dto.CantidadRequestDTO;
import com.tienda.productos.dto.DisponibilidadResponseDTO;
import com.tienda.productos.dto.ProductoRequestDTO;
import com.tienda.productos.dto.ProductoResponseDTO;
import com.tienda.productos.dto.ProductoUpdateDTO;
import com.tienda.productos.dto.StockRequestDTO;
import com.tienda.productos.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de gestion del catalogo de productos.
 */
@RestController
@RequestMapping("/productos")
@Tag(name = "Productos", description = "Gestion del catalogo de productos de la tienda virtual")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo producto")
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto));
    }

    @GetMapping
    @Operation(summary = "Listar productos activos con filtros opcionales por nombre y categoria")
    public ResponseEntity<List<ProductoResponseDTO>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(productoService.listar(nombre, categoria));
    }

    @GetMapping("/categorias")
    @Operation(summary = "Listar las categorias distintas de los productos activos")
    public ResponseEntity<List<String>> listarCategorias() {
        return ResponseEntity.ok(productoService.listarCategorias());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un producto por su ID")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar los datos de un producto", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoUpdateDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar un producto (baja logica)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Fijar el stock de un producto en un valor absoluto", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductoResponseDTO> fijarStock(@PathVariable Long id, @Valid @RequestBody StockRequestDTO dto) {
        return ResponseEntity.ok(productoService.fijarStock(id, dto.getStock()));
    }

    @PostMapping("/{id}/reservar")
    @Operation(summary = "Reservar stock de un producto (uso interno)")
    public ResponseEntity<ProductoResponseDTO> reservar(@PathVariable Long id, @Valid @RequestBody CantidadRequestDTO dto) {
        return ResponseEntity.ok(productoService.reservar(id, dto.getCantidad()));
    }

    @PostMapping("/{id}/liberar")
    @Operation(summary = "Liberar stock de un producto (uso interno)")
    public ResponseEntity<ProductoResponseDTO> liberar(@PathVariable Long id, @Valid @RequestBody CantidadRequestDTO dto) {
        return ResponseEntity.ok(productoService.liberar(id, dto.getCantidad()));
    }

    @GetMapping("/{id}/disponibilidad")
    @Operation(summary = "Consultar la disponibilidad de un producto para una cantidad")
    public ResponseEntity<DisponibilidadResponseDTO> consultarDisponibilidad(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(productoService.consultarDisponibilidad(id, cantidad));
    }
}

package com.tienda.productos.service;

import com.tienda.productos.dto.DisponibilidadResponseDTO;
import com.tienda.productos.dto.ProductoRequestDTO;
import com.tienda.productos.dto.ProductoResponseDTO;
import com.tienda.productos.dto.ProductoUpdateDTO;
import com.tienda.productos.exception.BusinessException;
import com.tienda.productos.exception.ResourceNotFoundException;
import com.tienda.productos.model.Producto;
import com.tienda.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reglas de negocio para la gestion del catalogo de productos.
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * Registra un nuevo producto activo validando precio y stock.
     */
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        validarPrecioYStock(dto.getPrecio(), dto.getStock());

        Producto producto = new Producto();
        producto.setNombre(dto.getNombre().trim());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setImagen(dto.getImagen());
        producto.setCategoria(dto.getCategoria().trim());
        producto.setEstado(Boolean.TRUE);

        Producto guardado = productoRepository.save(producto);
        return mapear(guardado);
    }

    /**
     * Lista los productos activos aplicando filtros opcionales por nombre y categoria.
     */
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listar(String nombre, String categoria) {
        boolean tieneNombre = nombre != null && !nombre.isBlank();
        boolean tieneCategoria = categoria != null && !categoria.isBlank();

        List<Producto> productos;
        if (tieneNombre && tieneCategoria) {
            productos = productoRepository.findByEstadoTrueAndNombreContainingIgnoreCaseAndCategoriaIgnoreCase(
                    nombre.trim(), categoria.trim());
        } else if (tieneNombre) {
            productos = productoRepository.findByEstadoTrueAndNombreContainingIgnoreCase(nombre.trim());
        } else if (tieneCategoria) {
            productos = productoRepository.findByEstadoTrueAndCategoriaIgnoreCase(categoria.trim());
        } else {
            productos = productoRepository.findByEstadoTrue();
        }

        return productos.stream().map(this::mapear).toList();
    }

    /**
     * Obtiene un producto por su identificador, aunque este inactivo.
     */
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        return mapear(buscarOFallar(id));
    }

    /**
     * Actualiza los datos editables de un producto.
     */
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoUpdateDTO dto) {
        validarPrecioYStock(dto.getPrecio(), dto.getStock());

        Producto producto = buscarOFallar(id);
        producto.setNombre(dto.getNombre().trim());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setImagen(dto.getImagen());
        producto.setCategoria(dto.getCategoria().trim());

        return mapear(productoRepository.save(producto));
    }

    /**
     * Realiza la baja logica de un producto (estado = false).
     */
    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarOFallar(id);
        producto.setEstado(Boolean.FALSE);
        productoRepository.save(producto);
    }

    /**
     * Fija el stock de un producto en el valor absoluto indicado (uso administrativo).
     */
    @Transactional
    public ProductoResponseDTO fijarStock(Long id, Integer stock) {
        if (stock == null || stock < 0) {
            throw new BusinessException("El stock no puede ser negativo");
        }
        Producto producto = buscarOFallar(id);
        producto.setStock(stock);
        return mapear(productoRepository.save(producto));
    }

    /**
     * Reserva (descuenta) stock de un producto de forma atomica.
     * Uso interno desde el microservicio de pedidos.
     */
    @Transactional
    public ProductoResponseDTO reservar(Long id, Integer cantidad) {
        validarCantidad(cantidad);

        Producto producto = buscarOFallar(id);
        if (Boolean.FALSE.equals(producto.getEstado())) {
            throw new BusinessException("El producto no esta disponible para la venta");
        }

        int filas = productoRepository.reservarStock(id, cantidad);
        if (filas == 0) {
            throw new BusinessException("Stock insuficiente para el producto " + producto.getNombre());
        }

        return mapear(buscarOFallar(id));
    }

    /**
     * Libera (devuelve) stock de un producto.
     * Uso interno desde el microservicio de pedidos.
     */
    @Transactional
    public ProductoResponseDTO liberar(Long id, Integer cantidad) {
        validarCantidad(cantidad);

        buscarOFallar(id);
        productoRepository.liberarStock(id, cantidad);

        return mapear(buscarOFallar(id));
    }

    /**
     * Lista las categorias distintas de los productos activos.
     */
    @Transactional(readOnly = true)
    public List<String> listarCategorias() {
        return productoRepository.listarCategorias();
    }

    /**
     * Consulta la disponibilidad de un producto para una cantidad dada.
     */
    @Transactional(readOnly = true)
    public DisponibilidadResponseDTO consultarDisponibilidad(Long id, Integer cantidad) {
        if (cantidad == null || cantidad < 0) {
            throw new BusinessException("La cantidad no puede ser negativa");
        }

        Producto producto = buscarOFallar(id);
        int stockActual = producto.getStock() != null ? producto.getStock() : 0;
        boolean disponible = Boolean.TRUE.equals(producto.getEstado()) && stockActual >= cantidad;

        return new DisponibilidadResponseDTO(producto.getIdProducto(), stockActual, disponible);
    }

    // ----------------------------- Metodos de apoyo -----------------------------

    public Producto buscarOFallar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id " + id));
    }

    private void validarPrecioYStock(BigDecimal precio, Integer stock) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio debe ser mayor que cero");
        }
        if (stock == null || stock < 0) {
            throw new BusinessException("El stock no puede ser negativo");
        }
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new BusinessException("La cantidad debe ser mayor que cero");
        }
    }

    private ProductoResponseDTO mapear(Producto producto) {
        return new ProductoResponseDTO(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getImagen(),
                producto.getCategoria(),
                producto.getEstado(),
                producto.getFechaRegistro());
    }
}

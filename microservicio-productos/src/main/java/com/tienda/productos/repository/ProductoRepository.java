package com.tienda.productos.repository;

import com.tienda.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos de productos.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByEstadoTrue();

    List<Producto> findByEstadoTrueAndCategoriaIgnoreCase(String categoria);

    List<Producto> findByEstadoTrueAndNombreContainingIgnoreCase(String nombre);

    List<Producto> findByEstadoTrueAndNombreContainingIgnoreCaseAndCategoriaIgnoreCase(String nombre, String categoria);

    @Query("select distinct p.categoria from Producto p where p.estado = true order by p.categoria")
    List<String> listarCategorias();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Producto p set p.stock = p.stock - :cantidad where p.idProducto = :id and p.stock >= :cantidad")
    int reservarStock(@Param("id") Long id, @Param("cantidad") Integer cantidad);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Producto p set p.stock = p.stock + :cantidad where p.idProducto = :id")
    int liberarStock(@Param("id") Long id, @Param("cantidad") Integer cantidad);
}

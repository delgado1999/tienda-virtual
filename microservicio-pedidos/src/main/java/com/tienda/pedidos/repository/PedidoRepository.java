package com.tienda.pedidos.repository;

import com.tienda.pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos de pedidos.
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByIdUsuarioOrderByFechaPedidoDesc(Long idUsuario);

    List<Pedido> findAllByOrderByFechaPedidoDesc();
}

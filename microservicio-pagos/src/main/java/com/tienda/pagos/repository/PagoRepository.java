package com.tienda.pagos.repository;

import com.tienda.pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos de pagos.
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByIdPedidoOrderByFechaPagoDesc(Long idPedido);
}

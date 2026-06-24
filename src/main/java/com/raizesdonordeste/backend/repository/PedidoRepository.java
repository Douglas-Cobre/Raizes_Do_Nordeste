package com.raizesdonordeste.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.backend.entity.Pedido;
import com.raizesdonordeste.backend.enums.CanalPedido;
import com.raizesdonordeste.backend.enums.StatusPedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteId(Long clienteId);

    Page<Pedido> findByClienteId(Long clienteId, Pageable pageable);

    Page<Pedido> findByClienteIdAndCanalPedido(
            Long clienteId,
            CanalPedido canalPedido,
            Pageable pageable);

    List<Pedido> findByStatus(StatusPedido status);

    List<Pedido> findByCanalPedido(CanalPedido canalPedido);

    Page<Pedido> findByCanalPedido(CanalPedido canalPedido, Pageable pageable);

}

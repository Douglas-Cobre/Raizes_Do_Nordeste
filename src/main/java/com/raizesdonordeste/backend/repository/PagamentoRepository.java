package com.raizesdonordeste.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.backend.entity.Pagamento;
import com.raizesdonordeste.backend.enums.StatusPagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByStatus(StatusPagamento status);

    Optional<Pagamento> findByPedidoId(Long pedidoId);

}

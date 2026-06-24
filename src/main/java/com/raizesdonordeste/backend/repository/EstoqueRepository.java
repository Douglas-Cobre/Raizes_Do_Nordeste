package com.raizesdonordeste.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.backend.entity.Estoque;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByProdutoIdAndUnidadeId(Long produtoId,Long unidadeId);

    List<Estoque> findByQuantidadeLessThan(Integer quantidade);
}

package com.raizesdonordeste.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.backend.entity.ProdutoUnidade;

public interface ProdutoUnidadeRepository extends JpaRepository<ProdutoUnidade, Long> {

    List<ProdutoUnidade> findByUnidadeId(Long unidadeId);

    List<ProdutoUnidade> findByUnidadeIdAndDisponivelTrue(Long unidadeId);

    Optional<ProdutoUnidade> findByProdutoIdAndUnidadeId(Long produtoId, Long unidadeId);
}

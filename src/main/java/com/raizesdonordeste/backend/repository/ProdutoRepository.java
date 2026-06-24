package com.raizesdonordeste.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.backend.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByDisponivelTrue();

    List<Produto> findByCategoria(String categoria);

}
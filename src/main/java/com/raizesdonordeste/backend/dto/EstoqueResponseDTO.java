package com.raizesdonordeste.backend.dto;

import com.raizesdonordeste.backend.entity.Estoque;

public record EstoqueResponseDTO(
        Long id,
        Integer quantidade,
        Integer estoqueMinimo,
        ProdutoResumoDTO produto,
        UnidadeResponseDTO unidade) {

    public static EstoqueResponseDTO from(Estoque estoque) {
        if (estoque == null) {
            return null;
        }

        return new EstoqueResponseDTO(
                estoque.getId(),
                estoque.getQuantidade(),
                estoque.getEstoqueMinimo(),
                ProdutoResumoDTO.from(estoque.getProduto()),
                UnidadeResponseDTO.from(estoque.getUnidade()));
    }
}

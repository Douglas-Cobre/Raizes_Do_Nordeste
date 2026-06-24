package com.raizesdonordeste.backend.dto;

import com.raizesdonordeste.backend.entity.Produto;

public record ProdutoResumoDTO(
        Long id,
        String nome,
        String descricao,
        Boolean disponivel,
        String categoria,
        Double preco) {

    public static ProdutoResumoDTO from(Produto produto) {
        if (produto == null) {
            return null;
        }

        return new ProdutoResumoDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getDisponivel(),
                produto.getCategoria(),
                produto.getPreco());
    }
}

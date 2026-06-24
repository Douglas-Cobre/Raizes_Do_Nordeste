package com.raizesdonordeste.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardapioItemDTO(
        Long produtoId,
        Long unidadeId,
        String nomeProduto,
        String descricaoProduto,
        String categoria,
        BigDecimal preco,
        Boolean disponivel,
        String observacaoRegional,
        LocalDate dataInicioDisponibilidade,
        LocalDate dataFimDisponibilidade) {
}

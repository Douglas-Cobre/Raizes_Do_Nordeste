package com.raizesdonordeste.backend.dto;

import java.math.BigDecimal;

public record RelatorioProdutoMaisConsumidoDTO(
        Long produtoId,
        String nomeProduto,
        Long quantidadeVendida,
        BigDecimal faturamento) {
}

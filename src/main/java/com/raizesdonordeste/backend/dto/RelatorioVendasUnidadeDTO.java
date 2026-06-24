package com.raizesdonordeste.backend.dto;

import java.math.BigDecimal;

public record RelatorioVendasUnidadeDTO(
        Long unidadeId,
        String unidadeNome,
        Long quantidadePedidos,
        BigDecimal faturamento) {
}

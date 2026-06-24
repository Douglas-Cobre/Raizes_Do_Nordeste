package com.raizesdonordeste.backend.dto;

import java.math.BigDecimal;

public record RelatorioVendasRegiaoDTO(
        String regiao,
        Long quantidadePedidos,
        BigDecimal faturamento) {
}

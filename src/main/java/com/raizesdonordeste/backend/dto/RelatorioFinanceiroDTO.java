package com.raizesdonordeste.backend.dto;

import java.math.BigDecimal;

public record RelatorioFinanceiroDTO(
        Long totalPedidos,
        Long totalPagamentos,
        Long pagamentosAprovados,
        Long pagamentosCancelados,
        BigDecimal faturamentoTotal,
        BigDecimal ticketMedio) {
}

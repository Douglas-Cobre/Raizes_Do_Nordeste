package com.raizesdonordeste.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import com.raizesdonordeste.backend.entity.Estoque;

public record PainelGerencialDTO(
        Long pedidosMesAtual,
        Long pedidosCanceladosMes,
        Long pagamentosAprovadosMes,
        Long pagamentosCanceladosMes,
        BigDecimal faturamentoMesAtual,
        BigDecimal descontoTotalMes,
        BigDecimal ticketMedioMes,
        Long estoqueBaixoQuantidade,
        Double percentualMetaPedidos,
        Double percentualMetaFaturamento,
        List<RelatorioVendasUnidadeDTO> vendasPorUnidade,
        List<RelatorioVendasRegiaoDTO> vendasPorRegiao,
        List<RelatorioProdutoMaisConsumidoDTO> produtosMaisConsumidos,
        List<Estoque> estoqueBaixo) {
}

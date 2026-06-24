package com.raizesdonordeste.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.raizesdonordeste.backend.dto.PainelGerencialDTO;
import com.raizesdonordeste.backend.dto.RelatorioFinanceiroDTO;
import com.raizesdonordeste.backend.dto.RelatorioProdutoMaisConsumidoDTO;
import com.raizesdonordeste.backend.dto.RelatorioVendasRegiaoDTO;
import com.raizesdonordeste.backend.dto.RelatorioVendasUnidadeDTO;
import com.raizesdonordeste.backend.entity.Estoque;
import com.raizesdonordeste.backend.entity.ItemPedido;
import com.raizesdonordeste.backend.entity.Pagamento;
import com.raizesdonordeste.backend.entity.Pedido;
import com.raizesdonordeste.backend.enums.StatusPagamento;
import com.raizesdonordeste.backend.enums.StatusPedido;
import com.raizesdonordeste.backend.repository.EstoqueRepository;
import com.raizesdonordeste.backend.repository.ItemPedidoRepository;
import com.raizesdonordeste.backend.repository.PagamentoRepository;
import com.raizesdonordeste.backend.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final PedidoRepository pedidoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final EstoqueRepository estoqueRepository;

    @Value("${app.kpi.meta-pedidos-mensal:100}")
    private Long metaPedidosMensal;

    @Value("${app.kpi.meta-faturamento-mensal:10000}")
    private BigDecimal metaFaturamentoMensal;

    public List<RelatorioVendasUnidadeDTO> vendasPorUnidade() {
        return pedidosDoMesAtual().stream()
                .collect(Collectors.groupingBy(
                        pedido -> pedido.getUnidade(),
                        Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> {
                    var unidade = entry.getKey();
                    var pedidos = entry.getValue();
                    BigDecimal faturamento = pedidos.stream()
                            .filter(p -> p.getTotal() != null)
                            .map(Pedido::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new RelatorioVendasUnidadeDTO(
                            unidade != null ? unidade.getId() : null,
                            unidade != null ? unidade.getNome() : null,
                            (long) pedidos.size(),
                            faturamento);
                })
                .sorted(Comparator.comparing(RelatorioVendasUnidadeDTO::faturamento).reversed())
                .toList();
    }

    public List<RelatorioVendasRegiaoDTO> vendasPorRegiao() {
        return pedidosDoMesAtual().stream()
                .collect(Collectors.groupingBy(
                        pedido -> pedido.getUnidade() != null ? pedido.getUnidade().getEstado() : "SEM_REGIAO",
                        Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> {
                    BigDecimal faturamento = entry.getValue().stream()
                            .filter(p -> p.getTotal() != null)
                            .map(Pedido::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new RelatorioVendasRegiaoDTO(
                            entry.getKey(),
                            (long) entry.getValue().size(),
                            faturamento);
                })
                .sorted(Comparator.comparing(RelatorioVendasRegiaoDTO::faturamento).reversed())
                .toList();
    }

    public List<RelatorioProdutoMaisConsumidoDTO> produtosMaisConsumidos(int limit) {
        YearMonth mesAtual = YearMonth.now();
        Map<Long, List<ItemPedido>> agrupado = itemPedidoRepository.findAll().stream()
                .filter(item -> item.getPedido() != null && item.getPedido().getDataCriacao() != null)
                .filter(item -> YearMonth.from(item.getPedido().getDataCriacao()).equals(mesAtual))
                .collect(Collectors.groupingBy(item -> item.getProduto() != null ? item.getProduto().getId() : null));

        return agrupado.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .map(entry -> {
                    List<ItemPedido> itens = entry.getValue();
                    ItemPedido primeiro = itens.get(0);
                    long quantidadeVendida = itens.stream()
                            .map(ItemPedido::getQuantidade)
                            .filter(q -> q != null)
                            .mapToLong(Integer::longValue)
                            .sum();
                    BigDecimal faturamento = itens.stream()
                            .map(ItemPedido::getSubtotal)
                            .filter(v -> v != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new RelatorioProdutoMaisConsumidoDTO(
                            entry.getKey(),
                            primeiro.getProduto() != null ? primeiro.getProduto().getNome() : null,
                            quantidadeVendida,
                            faturamento);
                })
                .sorted(Comparator.comparing(RelatorioProdutoMaisConsumidoDTO::quantidadeVendida).reversed())
                .limit(limit)
                .toList();
    }

    public RelatorioFinanceiroDTO financeiro() {
        List<Pagamento> pagamentos = pagamentoRepository.findAll();
        long totalPagamentos = pagamentos.size();
        long aprovados = pagamentos.stream().filter(p -> p.getStatus() == StatusPagamento.APROVADO).count();
        long cancelados = pagamentos.stream().filter(p -> p.getStatus() == StatusPagamento.CANCELADO).count();
        BigDecimal faturamento = pagamentos.stream()
                .filter(p -> p.getStatus() == StatusPagamento.APROVADO)
                .map(Pagamento::getValor)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalPedidos = pedidoRepository.findAll().size();
        BigDecimal ticketMedio = totalPagamentos == 0
                ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(totalPagamentos), 2, RoundingMode.HALF_UP);

        return new RelatorioFinanceiroDTO(
                totalPedidos,
                totalPagamentos,
                aprovados,
                cancelados,
                faturamento,
                ticketMedio);
    }

    public List<Estoque> estoqueBaixo() {
        return estoqueRepository.findByQuantidadeLessThan(10);
    }

    public PainelGerencialDTO painelGerencial() {
        List<Pedido> pedidosMes = pedidosDoMesAtual();
        List<Pagamento> pagamentosMes = pagamentosDoMesAtual();

        long pedidosCanceladosMes = pedidosMes.stream()
                .filter(p -> p.getStatus() == StatusPedido.CANCELADO)
                .count();
        long pagamentosAprovadosMes = pagamentosMes.stream()
                .filter(p -> p.getStatus() == StatusPagamento.APROVADO)
                .count();
        long pagamentosCanceladosMes = pagamentosMes.stream()
                .filter(p -> p.getStatus() == StatusPagamento.CANCELADO)
                .count();

        BigDecimal faturamentoMes = pagamentosMes.stream()
                .filter(p -> p.getStatus() == StatusPagamento.APROVADO)
                .map(Pagamento::getValor)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal descontoTotalMes = pedidosMes.stream()
                .map(Pedido::getDesconto)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ticketMedioMes = pedidosMes.isEmpty()
                ? BigDecimal.ZERO
                : faturamentoMes.divide(BigDecimal.valueOf(pedidosMes.size()), 2, RoundingMode.HALF_UP);

        double percentualMetaPedidos = metaPedidosMensal == null || metaPedidosMensal == 0
                ? 0.0
                : (pedidosMes.size() * 100.0) / metaPedidosMensal;

        double percentualMetaFaturamento = metaFaturamentoMensal == null || BigDecimal.ZERO.compareTo(metaFaturamentoMensal) == 0
                ? 0.0
                : faturamentoMes.multiply(BigDecimal.valueOf(100))
                        .divide(metaFaturamentoMensal, 2, RoundingMode.HALF_UP)
                        .doubleValue();

        return new PainelGerencialDTO(
                (long) pedidosMes.size(),
                pedidosCanceladosMes,
                pagamentosAprovadosMes,
                pagamentosCanceladosMes,
                faturamentoMes,
                descontoTotalMes,
                ticketMedioMes,
                (long) estoqueBaixo().size(),
                percentualMetaPedidos,
                percentualMetaFaturamento,
                vendasPorUnidade(),
                vendasPorRegiao(),
                produtosMaisConsumidos(5),
                estoqueBaixo());
    }

    private List<Pedido> pedidosDoMesAtual() {
        YearMonth mesAtual = YearMonth.now();
        return pedidoRepository.findAll().stream()
                .filter(pedido -> pedido.getDataCriacao() != null)
                .filter(pedido -> YearMonth.from(pedido.getDataCriacao()).equals(mesAtual))
                .toList();
    }

    private List<Pagamento> pagamentosDoMesAtual() {
        YearMonth mesAtual = YearMonth.now();
        return pagamentoRepository.findAll().stream()
                .filter(pagamento -> pagamento.getDataPagamento() != null)
                .filter(pagamento -> YearMonth.from(pagamento.getDataPagamento()).equals(mesAtual))
                .toList();
    }
}

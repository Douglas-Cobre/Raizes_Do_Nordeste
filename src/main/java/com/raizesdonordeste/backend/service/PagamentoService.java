package com.raizesdonordeste.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raizesdonordeste.backend.dto.PagamentoDTO;
import com.raizesdonordeste.backend.dto.PagamentoMockDTO;
import com.raizesdonordeste.backend.entity.Pagamento;
import com.raizesdonordeste.backend.entity.Pedido;
import com.raizesdonordeste.backend.enums.StatusPagamento;
import com.raizesdonordeste.backend.enums.StatusPedido;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.PagamentoRepository;
import com.raizesdonordeste.backend.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;
    private final AuditoriaService auditoriaService;

    public Pagamento criarPagamento(PagamentoDTO dto) {
        Long pedidoId = dto.pedidoIdResolvido();
        if (pedidoId == null) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "PEDIDO_OBRIGATORIO",
                    "pedidoId e obrigatorio.");
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "PEDIDO_NAO_ENCONTRADO",
                        "Pedido nao encontrado"));

        Pagamento pagamento = pagamentoRepository.findByPedidoId(pedidoId)
                .orElseGet(Pagamento::new);

        pagamento.setPedido(pedido);
        pagamento.setValor(dto.getValor());
        pagamento.setMetodoPagamento(dto.getMetodoPagamento());
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setDataPagamento(LocalDateTime.now());

        Pagamento salvo = pagamentoRepository.save(pagamento);
        auditoriaService.registrar(
                "CRIAR_PAGAMENTO",
                "Pagamento",
                null,
                resumoPagamento(salvo));
        return salvo;
    }

    @Transactional
    public Pagamento processarPagamentoMock(PagamentoMockDTO dto) {
        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "PEDIDO_NAO_ENCONTRADO",
                        "Pedido nao encontrado"));

        Pagamento pagamento = pagamentoRepository.findByPedidoId(dto.getPedidoId())
                .orElseGet(Pagamento::new);

        pagamento.setPedido(pedido);
        pagamento.setMetodoPagamento(dto.getMetodoPagamento());
        pagamento.setValor(valorFinalPedido(pedido));
        pagamento.setDataPagamento(LocalDateTime.now());
        pagamento.setCodigoTransacao("MOCK-" + UUID.randomUUID());

        if (Boolean.TRUE.equals(dto.getAprovado())) {
            pagamento.setStatus(StatusPagamento.APROVADO);
            pedido.setStatus(StatusPedido.PAGO);
        } else {
            pagamento.setStatus(StatusPagamento.RECUSADO);
            pedidoService.devolverEstoqueDoPedido(pedido);
            pedido.setStatus(StatusPedido.CANCELADO);
        }

        Pagamento salvo = pagamentoRepository.save(pagamento);
        auditoriaService.registrar(
                "PROCESSAR_PAGAMENTO_MOCK",
                "Pagamento",
                null,
                resumoPagamento(salvo) + "|aprovado=" + dto.getAprovado());
        auditoriaService.registrar(
                "ATUALIZAR_STATUS_PEDIDO",
                "Pedido",
                null,
                String.format("{id:%s,status:%s}", pedido.getId(), pedido.getStatus()));
        return salvo;
    }

    @Transactional
    public Pagamento aprovarPagamento(Long id) {
        Pagamento pagamento = buscarPagamento(id);

        pagamento.setStatus(StatusPagamento.APROVADO);

        Pedido pedido = pagamento.getPedido();
        pedido.setStatus(StatusPedido.PAGO);

        Pagamento salvo = pagamentoRepository.save(pagamento);
        auditoriaService.registrar(
                "APROVAR_PAGAMENTO",
                "Pagamento",
                null,
                resumoPagamento(salvo));
        return salvo;
    }

    @Transactional
    public Pagamento cancelarPagamento(Long id) {
        Pagamento pagamento = buscarPagamento(id);

        pagamento.setStatus(StatusPagamento.CANCELADO);

        Pedido pedido = pagamento.getPedido();
        pedidoService.devolverEstoqueDoPedido(pedido);
        pedido.setStatus(StatusPedido.CANCELADO);

        Pagamento salvo = pagamentoRepository.save(pagamento);
        auditoriaService.registrar(
                "CANCELAR_PAGAMENTO",
                "Pagamento",
                null,
                resumoPagamento(salvo));
        return salvo;
    }

    public List<Pagamento> listarPagamentosPorStatus(StatusPagamento status) {
        return pagamentoRepository.findByStatus(status);
    }

    public Pagamento buscarPagamento(Long id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "PAGAMENTO_NAO_ENCONTRADO",
                        "Pagamento nao encontrado"));
    }

    private BigDecimal valorFinalPedido(Pedido pedido) {
        BigDecimal total = pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO;
        BigDecimal desconto = pedido.getDesconto() != null ? pedido.getDesconto() : BigDecimal.ZERO;
        BigDecimal valor = total.subtract(desconto);
        return valor.signum() < 0 ? BigDecimal.ZERO : valor;
    }

    private String resumoPagamento(Pagamento pagamento) {
        return String.format(
                "{id:%s,status:%s,valor:%s,metodo:%s,pedidoId:%s,codigoTransacao:%s}",
                pagamento.getId(),
                pagamento.getStatus(),
                pagamento.getValor(),
                pagamento.getMetodoPagamento(),
                pagamento.getPedido() != null ? pagamento.getPedido().getId() : null,
                pagamento.getCodigoTransacao());
    }
}

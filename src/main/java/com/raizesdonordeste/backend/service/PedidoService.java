package com.raizesdonordeste.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raizesdonordeste.backend.dto.DescontoPedidoDTO;
import com.raizesdonordeste.backend.dto.ItemPedidoDTO;
import com.raizesdonordeste.backend.dto.PageResponseDTO;
import com.raizesdonordeste.backend.dto.PedidoDTO;
import com.raizesdonordeste.backend.entity.Cliente;
import com.raizesdonordeste.backend.entity.Fidelidade;
import com.raizesdonordeste.backend.entity.ItemPedido;
import com.raizesdonordeste.backend.entity.Pedido;
import com.raizesdonordeste.backend.entity.Produto;
import com.raizesdonordeste.backend.entity.ProdutoUnidade;
import com.raizesdonordeste.backend.entity.Unidade;
import com.raizesdonordeste.backend.enums.CanalPedido;
import com.raizesdonordeste.backend.enums.StatusPedido;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.ClienteRepository;
import com.raizesdonordeste.backend.repository.PedidoRepository;
import com.raizesdonordeste.backend.repository.ProdutoRepository;
import com.raizesdonordeste.backend.repository.ProdutoUnidadeRepository;
import com.raizesdonordeste.backend.repository.UnidadeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PedidoService {

    private static final Set<String> CAMPOS_ORDENACAO = Set.of(
            "id",
            "dataCriacao",
            "status",
            "canalPedido");

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final UnidadeRepository unidadeRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoUnidadeRepository produtoUnidadeRepository;
    private final EstoqueService estoqueService;
    private final AuditoriaService auditoriaService;
    private final FidelidadeService fidelidadeService;
    private final AuthorizationService authorizationService;

    @Transactional
    public Pedido criarPedido(PedidoDTO dto) {
        Long clienteId = dto.clienteIdResolvido();
        Long unidadeId = dto.unidadeIdResolvido();

        if (clienteId == null) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "CLIENTE_OBRIGATORIO",
                    "clienteId e obrigatorio.");
        }

        if (unidadeId == null) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "UNIDADE_OBRIGATORIA",
                    "unidadeId e obrigatorio.");
        }

        authorizationService.validarAcessoAoCliente(clienteId);

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "CLIENTE_NAO_ENCONTRADO",
                        "Cliente nao encontrado"));

        Unidade unidade = unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "UNIDADE_NAO_ENCONTRADA",
                        "Unidade nao encontrada"));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setUnidade(unidade);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setCanalPedido(dto.getCanalPedido());
        pedido.setDataCriacao(LocalDateTime.now());

        List<ItemPedido> itens = new ArrayList<>();
        BigDecimal totalPedido = BigDecimal.ZERO;

        for (ItemPedidoDTO itemDto : dto.getItens()) {
            Long produtoId = itemDto.produtoIdResolvido();
            if (produtoId == null) {
                throw new BusinessException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "PRODUTO_OBRIGATORIO",
                        "produtoId e obrigatorio para todos os itens.");
            }

            Produto produto = produtoRepository.findById(produtoId)
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.NOT_FOUND,
                            "PRODUTO_NAO_ENCONTRADO",
                            "Produto nao encontrado"));

            ProdutoUnidade produtoUnidade = produtoUnidadeRepository
                    .findByProdutoIdAndUnidadeId(produtoId, unidadeId)
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.CONFLICT,
                            "PRODUTO_INDISPONIVEL_UNIDADE",
                            "Produto indisponivel para a unidade informada"));

            if (!Boolean.TRUE.equals(produto.getDisponivel())
                    || !Boolean.TRUE.equals(produtoUnidade.getDisponivel())) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "PRODUTO_INDISPONIVEL_UNIDADE",
                        "Produto indisponivel para a unidade informada");
            }

            estoqueService.consultarEstoque(produtoId, unidadeId);

            BigDecimal precoUnitario = precoProduto(produto, produtoUnidade);
            BigDecimal subtotal = precoUnitario
                    .multiply(BigDecimal.valueOf(itemDto.getQuantidade()))
                    .setScale(2, RoundingMode.HALF_UP);

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(itemDto.getQuantidade());
            item.setPrecoUnitario(precoUnitario.setScale(2, RoundingMode.HALF_UP));
            item.setSubtotal(subtotal);

            itens.add(item);
            totalPedido = totalPedido.add(subtotal);
        }

        pedido.setTotal(totalPedido.setScale(2, RoundingMode.HALF_UP));
        pedido.setDesconto(calcularDesconto(dto, cliente, pedido.getTotal()));
        pedido.setItens(itens);

        Pedido salvo = pedidoRepository.save(pedido);

        for (ItemPedido item : itens) {
            estoqueService.baixarEstoque(
                    item.getProduto().getId(),
                    unidadeId,
                    item.getQuantidade());
        }

        auditoriaService.registrar(
                "CRIAR_PEDIDO",
                "Pedido",
                null,
                resumoPedido(salvo));
        return salvo;
    }

    public Pedido buscarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "PEDIDO_NAO_ENCONTRADO",
                        "Pedido nao encontrado"));
        authorizationService.validarAcessoAoPedido(pedido);
        return pedido;
    }

    public List<Pedido> listarPedidosCliente(Long clienteId) {
        authorizationService.validarAcessoAoCliente(clienteId);
        return pedidoRepository.findByClienteId(clienteId);
    }

    public PageResponseDTO<Pedido> listarPedidos(
            CanalPedido canalPedido,
            int page,
            int limit,
            String sort,
            String direction) {

        validarPaginacao(page, limit);

        if (!CAMPOS_ORDENACAO.contains(sort)) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "ORDENACAO_INVALIDA",
                    "Campo de ordenacao invalido. Use: id, dataCriacao, status ou canalPedido.");
        }

        String normalizedDirection = direction.toLowerCase(Locale.ROOT);
        if (!Set.of("asc", "desc").contains(normalizedDirection)) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "ORDENACAO_INVALIDA",
                    "direction deve ser asc ou desc.");
        }

        Pageable pageable = PageRequest.of(
                page,
                limit,
                Sort.by(Sort.Direction.fromString(normalizedDirection), sort));

        Page<Pedido> resultado;
        if (authorizationService.isCliente()) {
            Long clienteId = authorizationService.clienteIdAutenticado();
            resultado = canalPedido == null
                    ? pedidoRepository.findByClienteId(clienteId, pageable)
                    : pedidoRepository.findByClienteIdAndCanalPedido(
                            clienteId,
                            canalPedido,
                            pageable);
        } else {
            resultado = canalPedido == null
                    ? pedidoRepository.findAll(pageable)
                    : pedidoRepository.findByCanalPedido(canalPedido, pageable);
        }

        return PageResponseDTO.from(resultado);
    }

    public Pedido atualizarStatus(Long pedidoId, StatusPedido status) {
        Pedido pedido = buscarPedidoSemValidarAcesso(pedidoId);
        pedido.setStatus(status);

        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrar(
                "ATUALIZAR_STATUS_PEDIDO",
                "Pedido",
                null,
                resumoPedido(salvo));
        return salvo;
    }

    @Transactional
    public void cancelarPedido(Long pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);
        if (authorizationService.isCliente()
                && pedido.getStatus() != StatusPedido.CRIADO
                && pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "CANCELAMENTO_NAO_PERMITIDO",
                    "O cliente so pode cancelar pedidos ainda nao processados.");
        }
        devolverEstoqueDoPedido(pedido);
        pedido.setStatus(StatusPedido.CANCELADO);

        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrar(
                "CANCELAR_PEDIDO",
                "Pedido",
                null,
                resumoPedido(salvo));
    }

    public Pedido aplicarDesconto(Long pedidoId, DescontoPedidoDTO dto) {
        Pedido pedido = buscarPedidoSemValidarAcesso(pedidoId);
        BigDecimal valorDesconto = dto.getValorDesconto();
        if (valorDesconto.signum() < 0) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "DESCONTO_INVALIDO",
                    "O desconto deve ser maior ou igual a zero.");
        }

        BigDecimal total = pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO;
        if (valorDesconto.compareTo(total) > 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "DESCONTO_SUPERA_TOTAL",
                    "O desconto nao pode ser maior que o total do pedido.");
        }

        pedido.setDesconto(valorDesconto);
        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrar(
                "APLICAR_DESCONTO_PEDIDO",
                "Pedido",
                null,
                resumoPedido(salvo) + "|motivo=" + dto.getMotivo());
        return salvo;
    }

    public Pedido removerDesconto(Long pedidoId) {
        Pedido pedido = buscarPedidoSemValidarAcesso(pedidoId);
        pedido.setDesconto(BigDecimal.ZERO);
        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrar(
                "REMOVER_DESCONTO_PEDIDO",
                "Pedido",
                null,
                resumoPedido(salvo));
        return salvo;
    }

    public Pedido aplicarDescontoFidelidade(Long pedidoId, Long clienteId) {
        Pedido pedido = buscarPedidoSemValidarAcesso(pedidoId);
        Long clientePedido = pedido.getCliente() != null ? pedido.getCliente().getId() : null;
        if (!clienteId.equals(clientePedido)) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "CLIENTE_PEDIDO_DIVERGENTE",
                    "O cliente informado deve ser o titular do pedido.");
        }
        Fidelidade fidelidade = fidelidadeService.buscarFidelidadeCliente(clienteId);
        BigDecimal percentual = fidelidadeService.percentualDesconto(fidelidade);
        BigDecimal descontoCalculado = pedido.getTotal()
                .multiply(percentual)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        pedido.setDesconto(descontoCalculado);
        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrar(
                "APLICAR_DESCONTO_FIDELIDADE",
                "Pedido",
                null,
                resumoPedido(salvo) + "|nivel=" + fidelidade.getNivel());
        return salvo;
    }

    public void devolverEstoqueDoPedido(Pedido pedido) {
        if (pedido.getItens() == null || pedido.getStatus() == StatusPedido.CANCELADO) {
            return;
        }

        Long unidadeId = pedido.getUnidade() != null ? pedido.getUnidade().getId() : null;
        if (unidadeId == null) {
            return;
        }

        for (ItemPedido item : pedido.getItens()) {
            if (item.getProduto() != null && item.getQuantidade() != null) {
                estoqueService.reporEstoque(
                        item.getProduto().getId(),
                        unidadeId,
                        item.getQuantidade());
            }
        }
    }

    private BigDecimal calcularDesconto(PedidoDTO dto, Cliente cliente, BigDecimal totalPedido) {
        BigDecimal descontoBase = dto.getDesconto() != null ? dto.getDesconto() : BigDecimal.ZERO;
        if (descontoBase.signum() < 0 || descontoBase.compareTo(totalPedido) > 0) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "DESCONTO_INVALIDO",
                    "O desconto deve ser maior ou igual a zero e nao pode superar o total.");
        }

        BigDecimal percentualFidelidade = fidelidadeService.percentualDescontoDoCliente(cliente.getId());
        BigDecimal descontoFidelidade = totalPedido
                .multiply(percentualFidelidade)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return descontoBase.add(descontoFidelidade).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal precoProduto(Produto produto, ProdutoUnidade produtoUnidade) {
        if (produtoUnidade.getPreco() != null) {
            return produtoUnidade.getPreco();
        }

        if (produto.getPreco() != null) {
            return BigDecimal.valueOf(produto.getPreco());
        }

        throw new BusinessException(
                HttpStatus.CONFLICT,
                "PRECO_PRODUTO_INDISPONIVEL",
                "Produto sem preco cadastrado para venda.");
    }

    private String resumoPedido(Pedido pedido) {
        return String.format(
                "{id:%s,status:%s,canalPedido:%s,total:%s,desconto:%s,clienteId:%s,unidadeId:%s}",
                pedido.getId(),
                pedido.getStatus(),
                pedido.getCanalPedido(),
                pedido.getTotal(),
                pedido.getDesconto(),
                pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                pedido.getUnidade() != null ? pedido.getUnidade().getId() : null);
    }

    private void validarPaginacao(int page, int limit) {
        if (page < 0) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "PAGINACAO_INVALIDA",
                    "page deve ser maior ou igual a zero.");
        }

        if (limit < 1 || limit > 100) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "PAGINACAO_INVALIDA",
                    "limit deve estar entre 1 e 100.");
        }
    }

    private Pedido buscarPedidoSemValidarAcesso(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "PEDIDO_NAO_ENCONTRADO",
                        "Pedido nao encontrado"));
    }
}

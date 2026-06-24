package com.raizesdonordeste.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.raizesdonordeste.backend.dto.PageResponseDTO;
import com.raizesdonordeste.backend.entity.Pedido;
import com.raizesdonordeste.backend.enums.CanalPedido;
import com.raizesdonordeste.backend.repository.ClienteRepository;
import com.raizesdonordeste.backend.repository.PedidoRepository;
import com.raizesdonordeste.backend.repository.ProdutoRepository;
import com.raizesdonordeste.backend.repository.ProdutoUnidadeRepository;
import com.raizesdonordeste.backend.repository.UnidadeRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServicePaginacaoTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private UnidadeRepository unidadeRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ProdutoUnidadeRepository produtoUnidadeRepository;
    @Mock
    private EstoqueService estoqueService;
    @Mock
    private AuditoriaService auditoriaService;
    @Mock
    private FidelidadeService fidelidadeService;
    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void deveManterFiltroPorCanalNaConsultaPaginada() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setCanalPedido(CanalPedido.APP);

        when(pedidoRepository.findByCanalPedido(eq(CanalPedido.APP), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pedido), PageRequest.of(0, 10), 1));

        PageResponseDTO<Pedido> response = pedidoService.listarPedidos(
                CanalPedido.APP,
                0,
                10,
                "dataCriacao",
                "desc");

        assertThat(response.content())
                .allMatch(item -> item.getCanalPedido() == CanalPedido.APP);
        assertThat(response.page()).isZero();
        assertThat(response.limit()).isEqualTo(10);
        verify(pedidoRepository).findByCanalPedido(eq(CanalPedido.APP), any(Pageable.class));
    }
}

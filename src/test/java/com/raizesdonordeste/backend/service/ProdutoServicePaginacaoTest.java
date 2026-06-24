package com.raizesdonordeste.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import com.raizesdonordeste.backend.entity.Produto;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class ProdutoServicePaginacaoTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    void deveRetornarProdutosPaginadosEOrdenadosPorNome() {
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setNome("Baiao de Dois");

        when(produtoRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(produto), PageRequest.of(0, 10), 1));

        PageResponseDTO<Produto> response = produtoService.listarProdutos(0, 10, "nome", "asc");

        assertThat(response.content()).containsExactly(produto);
        assertThat(response.page()).isZero();
        assertThat(response.limit()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
        verify(produtoRepository).findAll(any(Pageable.class));
    }

    @Test
    void deveRejeitarParametrosDePaginacaoInvalidos() {
        assertThatThrownBy(() -> produtoService.listarProdutos(-1, 10, "nome", "asc"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("page");
        assertThatThrownBy(() -> produtoService.listarProdutos(0, 0, "nome", "asc"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("limit");
        assertThatThrownBy(() -> produtoService.listarProdutos(0, 101, "nome", "asc"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("limit");
    }

    @Test
    void deveRejeitarCampoDeOrdenacaoNaoPermitido() {
        assertThatThrownBy(() -> produtoService.listarProdutos(0, 10, "descricao", "asc"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Campo de ordenacao invalido");
    }
}

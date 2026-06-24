package com.raizesdonordeste.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.raizesdonordeste.backend.dto.CardapioItemDTO;
import com.raizesdonordeste.backend.entity.ProdutoUnidade;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.ProdutoUnidadeRepository;
import com.raizesdonordeste.backend.repository.UnidadeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardapioService {

    private final ProdutoUnidadeRepository produtoUnidadeRepository;
    private final UnidadeRepository unidadeRepository;

    public List<CardapioItemDTO> listarCardapioDaUnidade(Long unidadeId) {
        validarUnidade(unidadeId);
        return produtoUnidadeRepository.findByUnidadeId(unidadeId).stream()
                .filter(this::estaDisponivelHoje)
                .map(this::toDTO)
                .toList();
    }

    public List<CardapioItemDTO> listarProdutosDisponiveis(Long unidadeId) {
        validarUnidade(unidadeId);
        return produtoUnidadeRepository.findByUnidadeIdAndDisponivelTrue(unidadeId).stream()
                .filter(this::estaDisponivelHoje)
                .map(this::toDTO)
                .toList();
    }

    private void validarUnidade(Long unidadeId) {
        if (unidadeId == null) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "UNIDADE_OBRIGATORIA",
                    "Unidade é obrigatória.");
        }

        unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "UNIDADE_NAO_ENCONTRADA",
                        "Unidade não encontrada."));
    }

    private boolean estaDisponivelHoje(ProdutoUnidade produtoUnidade) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = produtoUnidade.getDataInicioDisponibilidade();
        LocalDate fim = produtoUnidade.getDataFimDisponibilidade();

        boolean iniciou = inicio == null || !hoje.isBefore(inicio);
        boolean naoExpirou = fim == null || !hoje.isAfter(fim);
        return Boolean.TRUE.equals(produtoUnidade.getDisponivel()) && iniciou && naoExpirou;
    }

    private CardapioItemDTO toDTO(ProdutoUnidade produtoUnidade) {
        return new CardapioItemDTO(
                produtoUnidade.getProduto() != null ? produtoUnidade.getProduto().getId() : null,
                produtoUnidade.getUnidade() != null ? produtoUnidade.getUnidade().getId() : null,
                produtoUnidade.getProduto() != null ? produtoUnidade.getProduto().getNome() : null,
                produtoUnidade.getProduto() != null ? produtoUnidade.getProduto().getDescricao() : null,
                produtoUnidade.getProduto() != null ? produtoUnidade.getProduto().getCategoria() : null,
                produtoUnidade.getPreco(),
                produtoUnidade.getDisponivel(),
                produtoUnidade.getObservacaoRegional(),
                produtoUnidade.getDataInicioDisponibilidade(),
                produtoUnidade.getDataFimDisponibilidade());
    }
}

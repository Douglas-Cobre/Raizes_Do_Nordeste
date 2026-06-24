package com.raizesdonordeste.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.raizesdonordeste.backend.dto.ProdutoUnidadeDTO;
import com.raizesdonordeste.backend.entity.Produto;
import com.raizesdonordeste.backend.entity.ProdutoUnidade;
import com.raizesdonordeste.backend.entity.Unidade;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.ProdutoRepository;
import com.raizesdonordeste.backend.repository.ProdutoUnidadeRepository;
import com.raizesdonordeste.backend.repository.UnidadeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoUnidadeService {

    private final ProdutoUnidadeRepository produtoUnidadeRepository;
    private final ProdutoRepository produtoRepository;
    private final UnidadeRepository unidadeRepository;

    public ProdutoUnidade criarVinculo(ProdutoUnidadeDTO dto) {
        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "PRODUTO_NAO_ENCONTRADO",
                        "Produto não encontrado."));

        Unidade unidade = unidadeRepository.findById(dto.getUnidadeId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "UNIDADE_NAO_ENCONTRADA",
                        "Unidade não encontrada."));

        ProdutoUnidade vinculo = new ProdutoUnidade();
        vinculo.setProduto(produto);
        vinculo.setUnidade(unidade);
        vinculo.setPreco(dto.getPreco());
        vinculo.setDisponivel(dto.getDisponivel());
        vinculo.setObservacaoRegional(dto.getObservacaoRegional());
        vinculo.setDataInicioDisponibilidade(dto.getDataInicioDisponibilidade());
        vinculo.setDataFimDisponibilidade(dto.getDataFimDisponibilidade());

        return produtoUnidadeRepository.save(vinculo);
    }

    public List<ProdutoUnidade> listarPorUnidade(Long unidadeId) {
        if (unidadeId == null) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "UNIDADE_OBRIGATORIA",
                    "Unidade é obrigatória.");
        }
        return produtoUnidadeRepository.findByUnidadeId(unidadeId);
    }
}

package com.raizesdonordeste.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.raizesdonordeste.backend.dto.EstoqueDTO;
import com.raizesdonordeste.backend.dto.AjusteEstoqueDTO;
import com.raizesdonordeste.backend.entity.Estoque;
import com.raizesdonordeste.backend.entity.Produto;
import com.raizesdonordeste.backend.entity.Unidade;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.EstoqueRepository;
import com.raizesdonordeste.backend.repository.ProdutoRepository;
import com.raizesdonordeste.backend.repository.UnidadeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;

    private final ProdutoRepository produtoRepository;

    private final UnidadeRepository unidadeRepository;

    private final AuditoriaService auditoriaService;

    public Estoque cadastrarEstoque(EstoqueDTO dto) {

        Produto produto = produtoRepository.findById(dto.getProduto().getId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "PRODUTO_NAO_ENCONTRADO",
                        "Produto não encontrado"));

        Unidade unidade = unidadeRepository.findById(dto.getUnidade().getId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "UNIDADE_NAO_ENCONTRADA",
                        "Unidade não encontrada"));

        Estoque estoque = new Estoque();

        estoque.setProduto(produto);

        estoque.setUnidade(unidade);

        estoque.setQuantidade(dto.getQuantidade());

        Estoque salvo = estoqueRepository.save(estoque);
        auditoriaService.registrar(
                "CRIAR_ESTOQUE",
                "Estoque",
                null,
                resumoEstoque(salvo));
        return salvo;
    }

    public Estoque consultarEstoque(Long produtoId,
            Long unidadeId) {

        return estoqueRepository
                .findByProdutoIdAndUnidadeId(
                        produtoId,
                        unidadeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "ESTOQUE_NAO_ENCONTRADO",
                        "Estoque não encontrado"));
    }

    public Estoque baixarEstoque(Long produtoId,
            Long unidadeId,
            Integer quantidade) {

        Estoque estoque = consultarEstoque(produtoId, unidadeId);

        if (estoque.getQuantidade() < quantidade) {

            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "ESTOQUE_INSUFICIENTE",
                    "Estoque insuficiente");
        }

        estoque.setQuantidade(
                estoque.getQuantidade() - quantidade);

        Estoque salvo = estoqueRepository.save(estoque);
        auditoriaService.registrar(
                "BAIXAR_ESTOQUE",
                "Estoque",
                null,
                resumoEstoque(salvo));
        return salvo;
    }

    public Estoque reporEstoque(Long produtoId,
            Long unidadeId,
            Integer quantidade) {

        Estoque estoque = consultarEstoque(produtoId, unidadeId);

        estoque.setQuantidade(
                estoque.getQuantidade() + quantidade);

        Estoque salvo = estoqueRepository.save(estoque);
        auditoriaService.registrar(
                "REPOR_ESTOQUE",
                "Estoque",
                null,
                resumoEstoque(salvo));
        return salvo;
    }

    public List<Estoque> listarEstoqueBaixo() {

        return estoqueRepository
                .findByQuantidadeLessThan(10);
    }

    public Estoque ajustarEstoque(AjusteEstoqueDTO dto) {
        Estoque estoque = consultarEstoque(dto.getProdutoId(), dto.getUnidadeId());

        if (dto.getNovoSaldo() == null || dto.getNovoSaldo() < 0) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "SALDO_INVALIDO",
                    "O novo saldo deve ser maior ou igual a zero.");
        }

        Integer saldoAnterior = estoque.getQuantidade();
        estoque.setQuantidade(dto.getNovoSaldo());

        Estoque salvo = estoqueRepository.save(estoque);
        auditoriaService.registrar(
                "AJUSTAR_ESTOQUE",
                "Estoque",
                String.valueOf(saldoAnterior),
                resumoEstoque(salvo) + "|motivo=" + dto.getMotivo());
        return salvo;
    }

    private String resumoEstoque(Estoque estoque) {
        return String.format(
                "{id:%s,produtoId:%s,unidadeId:%s,quantidade:%s,estoqueMinimo:%s}",
                estoque.getId(),
                estoque.getProduto() != null ? estoque.getProduto().getId() : null,
                estoque.getUnidade() != null ? estoque.getUnidade().getId() : null,
                estoque.getQuantidade(),
                estoque.getEstoqueMinimo());
    }
}

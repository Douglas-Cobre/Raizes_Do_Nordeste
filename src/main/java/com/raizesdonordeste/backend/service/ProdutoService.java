package com.raizesdonordeste.backend.service;

import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.raizesdonordeste.backend.dto.PageResponseDTO;
import com.raizesdonordeste.backend.dto.ProdutoDTO;
import com.raizesdonordeste.backend.entity.Produto;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private static final Set<String> CAMPOS_ORDENACAO = Set.of("id", "nome", "preco");

    private final ProdutoRepository produtoRepository;

    public Produto criarProduto(ProdutoDTO dto) {

        Produto produto = new Produto();

        produto.setNome(dto.getNome());

        produto.setDescricao(dto.getDescricao());

        produto.setPreco(dto.getPreco());

        produto.setCategoria(dto.getCategoria());

        produto.setDisponivel(dto.getDisponivel());

        return produtoRepository.save(produto);
    }

    public Produto buscarProduto(Long id) {

        return produtoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "PRODUTO_NAO_ENCONTRADO",
                        "Produto não encontrado"));
    }

    public PageResponseDTO<Produto> listarProdutos(
            int page,
            int limit,
            String sort,
            String direction) {

        validarPaginacao(page, limit);

        if (!CAMPOS_ORDENACAO.contains(sort)) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "ORDENACAO_INVALIDA",
                    "Campo de ordenacao invalido. Use: id, nome ou preco.");
        }

        Sort.Direction sortDirection = validarDirecao(direction);
        Pageable pageable = PageRequest.of(
                page,
                limit,
                Sort.by(sortDirection, sort));

        return PageResponseDTO.from(produtoRepository.findAll(pageable));
    }

    public Produto atualizarProduto(Long id,
            ProdutoDTO dto) {

        Produto produto = buscarProduto(id);

        produto.setNome(dto.getNome());

        produto.setDescricao(dto.getDescricao());

        produto.setPreco(dto.getPreco());

        produto.setCategoria(dto.getCategoria());

        return produtoRepository.save(produto);
    }

    public Produto desativarProduto(Long id) {

        Produto produto = buscarProduto(id);

        produto.setDisponivel(false);

        return produtoRepository.save(produto);
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

    private Sort.Direction validarDirecao(String direction) {
        String normalizedDirection = direction.toLowerCase(Locale.ROOT);
        if (!Set.of("asc", "desc").contains(normalizedDirection)) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "ORDENACAO_INVALIDA",
                    "direction deve ser asc ou desc.");
        }

        return Sort.Direction.fromString(normalizedDirection);
    }

}

package com.raizesdonordeste.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.backend.dto.ProdutoUnidadeDTO;
import com.raizesdonordeste.backend.entity.ProdutoUnidade;
import com.raizesdonordeste.backend.service.ProdutoUnidadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/produto-unidades")
@RequiredArgsConstructor
@Tag(name = "Produtos por Unidade",
        description = "Vinculos, precos e disponibilidade regional de produtos por unidade")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class ProdutoUnidadeController {

    private final ProdutoUnidadeService produtoUnidadeService;

    @PostMapping
    @Operation(summary = "Vincular produto a unidade",
            description = "Configura preco, disponibilidade e periodo regional de um produto em uma unidade.")
    public ResponseEntity<ProdutoUnidade> criarVinculo(
            @Valid @RequestBody ProdutoUnidadeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(produtoUnidadeService.criarVinculo(dto));
    }

    @GetMapping("/unidade/{unidadeId}")
    @Operation(summary = "Listar produtos da unidade",
            description = "Retorna os vinculos de produtos configurados para a unidade.")
    public ResponseEntity<List<ProdutoUnidade>> listarPorUnidade(
            @Parameter(description = "Identificador da unidade", example = "1")
            @PathVariable Long unidadeId) {
        return ResponseEntity.ok(produtoUnidadeService.listarPorUnidade(unidadeId));
    }
}

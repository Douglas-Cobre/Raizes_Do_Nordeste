package com.raizesdonordeste.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.backend.dto.CardapioItemDTO;
import com.raizesdonordeste.backend.service.CardapioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/unidades")
@RequiredArgsConstructor
@Tag(name = "Cardápio", description = "Consulta do cardápio disponível por unidade")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class CardapioController {

    private final CardapioService cardapioService;

    @GetMapping("/{unidadeId}/cardapio")
    @Operation(summary = "Consultar cardápio da unidade", description = "Retorna os produtos configurados para uma unidade.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cardápio retornado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Unidade não encontrada")
    })
    public ResponseEntity<List<CardapioItemDTO>> listarCardapioDaUnidade(
            @Parameter(description = "Identificador da unidade", example = "1")
            @PathVariable("unidadeId") Long unidadeId) {
        return ResponseEntity.ok(cardapioService.listarCardapioDaUnidade(unidadeId));
    }

    @GetMapping("/{unidadeId}/produtos-disponiveis")
    @Operation(summary = "Listar produtos disponiveis",
            description = "Retorna apenas os itens disponiveis no cardapio da unidade. O parametro canalPedido e aceito pela rota, mas nao altera o filtro atual.")
    public ResponseEntity<List<CardapioItemDTO>> listarProdutosDisponiveis(
            @Parameter(description = "Identificador da unidade", example = "1")
            @PathVariable("unidadeId") Long unidadeId,
            @Parameter(description = "Canal informado pelo consumidor", example = "APP")
            @RequestParam(required = false) String canalPedido) {
        return ResponseEntity.ok(cardapioService.listarProdutosDisponiveis(unidadeId));
    }
}

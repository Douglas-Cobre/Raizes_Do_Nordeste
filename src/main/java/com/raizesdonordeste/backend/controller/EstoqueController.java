package com.raizesdonordeste.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.backend.dto.EstoqueDTO;
import com.raizesdonordeste.backend.dto.AjusteEstoqueDTO;
import com.raizesdonordeste.backend.dto.EstoqueResponseDTO;
import com.raizesdonordeste.backend.entity.Estoque;
import com.raizesdonordeste.backend.service.EstoqueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/estoque")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Consulta e movimentação de estoque por produto e unidade")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @PostMapping
    @Operation(summary = "Cadastrar estoque",
            description = "Cria o saldo inicial de um produto em uma unidade.")
    public ResponseEntity<Estoque> cadastrarEstoque(
            @Valid @RequestBody EstoqueDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(estoqueService.cadastrarEstoque(dto));
    }

    @GetMapping
    @Operation(summary = "Consultar estoque", description = "Consulta o saldo de um produto em uma unidade.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estoque encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado")
    })
    public ResponseEntity<EstoqueResponseDTO> consultarEstoque(
            @Parameter(description = "Identificador do produto", example = "1") @RequestParam("produtoId") Long produtoId,
            @Parameter(description = "Identificador da unidade", example = "1") @RequestParam("unidadeId") Long unidadeId) {

        return ResponseEntity.ok(EstoqueResponseDTO.from(
                estoqueService.consultarEstoque(
                        produtoId,
                        unidadeId)));
    }

    @PatchMapping("/baixar")
    @Operation(summary = "Baixar estoque", description = "Subtrai uma quantidade do saldo atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Baixa realizada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado"),
            @ApiResponse(responseCode = "409", description = "Estoque insuficiente")
    })
    public ResponseEntity<Estoque> baixarEstoque(
            @Parameter(description = "Identificador do produto", example = "1") @RequestParam("produtoId") Long produtoId,
            @Parameter(description = "Identificador da unidade", example = "1") @RequestParam("unidadeId") Long unidadeId,
            @Parameter(description = "Quantidade a baixar", example = "2") @RequestParam("quantidade") Integer quantidade) {

        return ResponseEntity.ok(
                estoqueService.baixarEstoque(
                        produtoId,
                        unidadeId,
                        quantidade));
    }

    @PatchMapping("/repor")
    @Operation(summary = "Repor estoque", description = "Soma uma quantidade ao saldo atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reposição realizada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado")
    })
    public ResponseEntity<Estoque> reporEstoque(
            @Parameter(description = "Identificador do produto", example = "1") @RequestParam("produtoId") Long produtoId,
            @Parameter(description = "Identificador da unidade", example = "1") @RequestParam("unidadeId") Long unidadeId,
            @Parameter(description = "Quantidade a repor", example = "5") @RequestParam("quantidade") Integer quantidade) {

        return ResponseEntity.ok(
                estoqueService.reporEstoque(
                        produtoId,
                        unidadeId,
                        quantidade));
    }

    @PatchMapping("/ajustar")
    @Operation(summary = "Ajustar estoque",
            description = "Substitui o saldo atual pelo novo saldo absoluto e registra o motivo informado.")
    public ResponseEntity<Estoque> ajustarEstoque(
            @Valid @RequestBody AjusteEstoqueDTO dto) {

        return ResponseEntity.ok(estoqueService.ajustarEstoque(dto));
    }

    @GetMapping("/baixo")
    @Operation(summary = "Listar estoque baixo", description = "Retorna estoques cuja quantidade é menor que 10.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estoques retornados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<List<Estoque>> listarEstoqueBaixo() {

        return ResponseEntity.ok(
                estoqueService.listarEstoqueBaixo());
    }

    @PatchMapping("/ajuste-gerencial")
    @Operation(summary = "Realizar ajuste gerencial",
            description = "Executa o mesmo ajuste absoluto de estoque pela rota gerencial.")
    public ResponseEntity<Estoque> ajusteGerencial(
            @Valid @RequestBody AjusteEstoqueDTO dto) {

        return ResponseEntity.ok(estoqueService.ajustarEstoque(dto));
    }

}

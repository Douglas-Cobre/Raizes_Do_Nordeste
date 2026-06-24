package com.raizesdonordeste.backend.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.raizesdonordeste.backend.entity.Fidelidade;
import com.raizesdonordeste.backend.exception.ApiErrorResponse;
import com.raizesdonordeste.backend.service.FidelidadeService;
import com.raizesdonordeste.backend.service.AuthorizationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/fidelidade")
@RequiredArgsConstructor
@Validated
@Tag(name = "Fidelidade", description = "Consulta e movimentação de pontos de fidelidade")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class FidelidadeController {

    private final FidelidadeService fidelidadeService;
    private final AuthorizationService authorizationService;

    @GetMapping("/{clienteId}")
    @Operation(summary = "Consultar fidelidade", description = "Retorna pontos e nível de fidelidade do cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fidelidade encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Fidelidade não encontrada")
    })
    public ResponseEntity<Fidelidade> buscarFidelidade(
            @Parameter(description = "Identificador do cliente", example = "1") @PathVariable("clienteId") Long clienteId) {

        authorizationService.validarAcessoAoCliente(clienteId);
        return ResponseEntity.ok(
                fidelidadeService
                        .buscarFidelidadeCliente(clienteId));
    }

    @PatchMapping("/{clienteId}/adicionar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Adicionar pontos", description = "Soma pontos ao cadastro de fidelidade do cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pontos adicionados"),
            @ApiResponse(responseCode = "422", description = "Quantidade menor ou igual a zero",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Fidelidade> adicionarPontos(
            @Parameter(description = "Identificador do cliente", example = "1") @PathVariable("clienteId") Long clienteId,
            @Parameter(description = "Pontos a adicionar", example = "100")
            @Positive(message = "pontos deve ser maior que zero") @RequestParam("pontos") Integer pontos) {

        return ResponseEntity.ok(
                fidelidadeService
                        .adicionarPontos(clienteId, pontos));
    }

    @PatchMapping("/{clienteId}/remover")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Remover pontos", description = "Subtrai pontos do cadastro de fidelidade do cliente, sem permitir saldo negativo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pontos removidos"),
            @ApiResponse(responseCode = "422", description = "Quantidade menor ou igual a zero",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Fidelidade> removerPontos(
            @Parameter(description = "Identificador do cliente", example = "1") @PathVariable("clienteId") Long clienteId,
            @Parameter(description = "Pontos a remover", example = "50")
            @Positive(message = "pontos deve ser maior que zero") @RequestParam("pontos") Integer pontos) {

        return ResponseEntity.ok(
                fidelidadeService
                        .removerPontos(clienteId, pontos));
    }

    @GetMapping("/{clienteId}/desconto")
    @Operation(summary = "Consultar desconto de fidelidade",
            description = "Retorna o percentual de desconto correspondente ao nivel atual do cliente.")
    public ResponseEntity<BigDecimal> consultarDesconto(
            @Parameter(description = "Identificador do cliente", example = "1")
            @PathVariable("clienteId") Long clienteId) {

        authorizationService.validarAcessoAoCliente(clienteId);
        return ResponseEntity.ok(
                fidelidadeService.percentualDescontoDoCliente(clienteId));
    }
}

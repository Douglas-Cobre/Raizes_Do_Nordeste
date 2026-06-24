package com.raizesdonordeste.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.backend.dto.PagamentoDTO;
import com.raizesdonordeste.backend.dto.PagamentoMockDTO;
import com.raizesdonordeste.backend.entity.Pagamento;
import com.raizesdonordeste.backend.enums.StatusPagamento;
import com.raizesdonordeste.backend.exception.ApiErrorResponse;
import com.raizesdonordeste.backend.service.PagamentoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pagamentos")
@RequiredArgsConstructor
@Tag(name = "Pagamentos", description = "Criacao, simulacao e consulta de pagamentos")
@SecurityRequirement(name = "bearerAuth")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @PostMapping
    @Operation(summary = "Criar pagamento",
            description = "Cria um pagamento pendente para um pedido, validando pedido, valor e metodo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pagamento criado",
                    content = @Content(schema = @Schema(implementation = Pagamento.class))),
            @ApiResponse(responseCode = "404", description = "Pedido nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Dados invalidos ou pedido sem identificador",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Pagamento> criarPagamento(@Valid @RequestBody PagamentoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pagamentoService.criarPagamento(dto));
    }

    @PostMapping("/mock")
    @Operation(summary = "Processar pagamento mock",
            description = "Simula aprovacao ou recusa, atualiza o pedido e devolve o estoque em caso de recusa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento processado",
                    content = @Content(schema = @Schema(implementation = Pagamento.class))),
            @ApiResponse(responseCode = "404", description = "Pedido nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Dados de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Pagamento> processarPagamentoMock(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = PagamentoMockDTO.class),
                            examples = {
                                    @ExampleObject(name = "Pagamento aprovado", value = """
                                            {"pedidoId":1,"metodoPagamento":"MOCK","aprovado":true}
                                            """),
                                    @ExampleObject(name = "Pagamento recusado", value = """
                                            {"pedidoId":1,"metodoPagamento":"MOCK","aprovado":false}
                                            """)
                            }))
            @Valid @RequestBody PagamentoMockDTO dto) {
        return ResponseEntity.ok(pagamentoService.processarPagamentoMock(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar pagamento", description = "Retorna um pagamento pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Pagamento nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Pagamento> buscarPagamento(
            @Parameter(description = "Identificador do pagamento", example = "1", required = true)
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(pagamentoService.buscarPagamento(id));
    }

    @PatchMapping("/{id}/aprovar")
    @Operation(summary = "Aprovar pagamento",
            description = "Marca o pagamento como aprovado e atualiza o pedido associado.")
    public ResponseEntity<Pagamento> aprovarPagamento(
            @Parameter(description = "Identificador do pagamento", example = "1") @PathVariable("id") Long id) {
        return ResponseEntity.ok(pagamentoService.aprovarPagamento(id));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar pagamento",
            description = "Cancela o pagamento e devolve ao estoque os itens do pedido associado.")
    public ResponseEntity<Pagamento> cancelarPagamento(
            @Parameter(description = "Identificador do pagamento", example = "1") @PathVariable("id") Long id) {
        return ResponseEntity.ok(pagamentoService.cancelarPagamento(id));
    }

    @GetMapping("/status")
    @Operation(summary = "Listar pagamentos por status",
            description = "Retorna os pagamentos que possuem o status informado.")
    public ResponseEntity<List<Pagamento>> buscarPorStatus(
            @Parameter(description = "Status do pagamento", example = "APROVADO",
                    schema = @Schema(allowableValues = {"PENDENTE", "APROVADO", "RECUSADO", "CANCELADO"}))
            @RequestParam("status") StatusPagamento status) {
        return ResponseEntity.ok(pagamentoService.listarPagamentosPorStatus(status));
    }
}

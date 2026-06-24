package com.raizesdonordeste.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.backend.dto.PainelGerencialDTO;
import com.raizesdonordeste.backend.dto.RelatorioFinanceiroDTO;
import com.raizesdonordeste.backend.dto.RelatorioProdutoMaisConsumidoDTO;
import com.raizesdonordeste.backend.dto.RelatorioVendasRegiaoDTO;
import com.raizesdonordeste.backend.dto.RelatorioVendasUnidadeDTO;
import com.raizesdonordeste.backend.entity.Estoque;
import com.raizesdonordeste.backend.service.RelatorioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Indicadores operacionais e gerenciais da rede")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/vendas-por-unidade")
    @Operation(summary = "Consultar vendas por unidade",
            description = "Agrupa quantidade de pedidos e faturamento por unidade.")
    public ResponseEntity<List<RelatorioVendasUnidadeDTO>> vendasPorUnidade() {
        return ResponseEntity.ok(relatorioService.vendasPorUnidade());
    }

    @GetMapping("/vendas-por-regiao")
    @Operation(summary = "Consultar vendas por regiao",
            description = "Agrupa quantidade de pedidos e faturamento por regiao.")
    public ResponseEntity<List<RelatorioVendasRegiaoDTO>> vendasPorRegiao() {
        return ResponseEntity.ok(relatorioService.vendasPorRegiao());
    }

    @GetMapping("/produtos-mais-consumidos")
    @Operation(summary = "Listar produtos mais consumidos",
            description = "Ordena os produtos por quantidade vendida e limita o resultado.")
    public ResponseEntity<List<RelatorioProdutoMaisConsumidoDTO>> produtosMaisConsumidos(
            @io.swagger.v3.oas.annotations.Parameter(description = "Quantidade maxima de itens", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(relatorioService.produtosMaisConsumidos(limit));
    }

    @GetMapping("/financeiro")
    @Operation(summary = "Consultar relatorio financeiro",
            description = "Consolida pedidos, pagamentos, faturamento e ticket medio.")
    public ResponseEntity<RelatorioFinanceiroDTO> financeiro() {
        return ResponseEntity.ok(relatorioService.financeiro());
    }

    @GetMapping("/estoque-baixo")
    @Operation(summary = "Consultar estoque baixo",
            description = "Retorna os saldos considerados baixos pelo relatorio gerencial.")
    public ResponseEntity<List<Estoque>> estoqueBaixo() {
        return ResponseEntity.ok(relatorioService.estoqueBaixo());
    }

    @GetMapping("/painel-gerencial")
    @Operation(summary = "Consultar painel gerencial", description = "Consolida pedidos, pagamentos, faturamento, metas, vendas e estoque baixo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Painel retornado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<PainelGerencialDTO> painelGerencial() {
        return ResponseEntity.ok(relatorioService.painelGerencial());
    }
}

package com.raizesdonordeste.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.backend.dto.PedidoDTO;
import com.raizesdonordeste.backend.dto.DescontoPedidoDTO;
import com.raizesdonordeste.backend.dto.PageResponseDTO;
import com.raizesdonordeste.backend.entity.Pedido;
import com.raizesdonordeste.backend.enums.CanalPedido;
import com.raizesdonordeste.backend.enums.StatusPedido;
import com.raizesdonordeste.backend.exception.ApiErrorResponse;
import com.raizesdonordeste.backend.service.PedidoService;

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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Pedidos", description = "Criação, consulta, atualização e cancelamento de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Criar pedido",
            description = "Valida cliente, unidade, disponibilidade e estoque. CLIENTE so pode criar pedido para o proprio cadastro.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado"),
            @ApiResponse(responseCode = "400",
                    description = "JSON ou valor enum invalido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Cliente, unidade ou produto não encontrado"),
            @ApiResponse(responseCode = "409", description = "Produto indisponível ou estoque insuficiente"),
            @ApiResponse(responseCode = "422", description = "Request inválido")
    })
    public ResponseEntity<Pedido> criarPedido(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Cliente, unidade, canal e itens do pedido",
                    content = @Content(schema = @Schema(implementation = PedidoDTO.class),
                            examples = @ExampleObject(name = "Pedido pelo aplicativo", value = """
                                    {
                                      "clienteId": 1,
                                      "unidadeId": 1,
                                      "canalPedido": "APP",
                                      "itens": [{"produtoId": 1, "quantidade": 2}]
                                    }
                                    """)))
            @Valid @RequestBody PedidoDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.criarPedido(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar pedido",
            description = "Retorna um pedido pelo ID. CLIENTE so pode consultar pedido proprio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<Pedido> buscarPedido(
            @Parameter(description = "Identificador do pedido", example = "1") @PathVariable("id") Long id) {

        return ResponseEntity.ok(
                pedidoService.buscarPedido(id));
    }

    @GetMapping
    @Operation(summary = "Listar ou filtrar pedidos",
            description = "ADMIN e GERENTE listam todos; CLIENTE recebe somente os proprios pedidos. O filtro por canal e opcional.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resposta paginada de pedidos",
                    content = @Content(schema = @Schema(implementation = PageResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Paginacao ou ordenacao invalida"),
            @ApiResponse(responseCode = "400",
                    description = "Parametro enum invalido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<PageResponseDTO<Pedido>> listarPedidos(
            @Parameter(description = "Canal do pedido", example = "APP",
                    schema = @Schema(allowableValues = {"APP", "TOTEM", "BALCAO", "PICKUP", "WEB"}))
            @RequestParam(name = "canalPedido", required = false) CanalPedido canalPedido,
            @Parameter(description = "Numero da pagina, iniciado em zero", example = "0")
            @Min(value = 0, message = "page deve ser maior ou igual a zero")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Quantidade de registros por pagina, entre 1 e 100", example = "10")
            @Min(value = 1, message = "limit deve ser maior ou igual a um")
            @Max(value = 100, message = "limit deve ser menor ou igual a cem")
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @Parameter(
                    description = "Campo usado para ordenacao",
                    example = "dataCriacao",
                    schema = @Schema(allowableValues = {"id", "dataCriacao", "status", "canalPedido"}))
            @RequestParam(name = "sort", defaultValue = "dataCriacao") String sort,
            @Parameter(
                    description = "Direcao da ordenacao",
                    example = "desc",
                    schema = @Schema(allowableValues = {"asc", "desc"}))
            @Pattern(regexp = "(?i)asc|desc", message = "direction deve ser asc ou desc")
            @RequestParam(name = "direction", defaultValue = "desc") String direction) {

        return ResponseEntity.ok(
                pedidoService.listarPedidos(canalPedido, page, limit, sort, direction));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar pedidos do cliente",
            description = "Retorna o historico do cliente informado. CLIENTE so pode consultar o proprio ID.")
    public ResponseEntity<List<Pedido>> listarPedidosCliente(
            @Parameter(description = "Identificador do cliente", example = "1", required = true)
            @PathVariable("clienteId") Long clienteId) {

        return ResponseEntity.ok(
                pedidoService.listarPedidosCliente(clienteId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Atualizar status do pedido",
            description = "Operacao gerencial restrita a ADMIN e GERENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "400", description = "Status inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<Pedido> atualizarStatus(
            @Parameter(description = "Identificador do pedido", example = "1") @PathVariable("id") Long id,
            @Parameter(description = "Novo status", example = "EM_PREPARO") @RequestParam("status") StatusPedido status) {

        return ResponseEntity.ok(
                pedidoService.atualizarStatus(id, status));
    }

    @PatchMapping("/{id}/desconto")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Aplicar desconto manual",
            description = "Operacao gerencial restrita a ADMIN e GERENTE.")
    public ResponseEntity<Pedido> aplicarDesconto(
            @PathVariable("id") Long id,
            @Valid @RequestBody DescontoPedidoDTO dto) {

        return ResponseEntity.ok(pedidoService.aplicarDesconto(id, dto));
    }

    @DeleteMapping("/{id}/desconto")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Remover desconto",
            description = "Operacao gerencial restrita a ADMIN e GERENTE.")
    public ResponseEntity<Pedido> removerDesconto(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(pedidoService.removerDesconto(id));
    }

    @PatchMapping("/{id}/desconto-fidelidade")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Aplicar desconto de fidelidade",
            description = "Operacao gerencial restrita a ADMIN e GERENTE; o cliente informado deve ser o titular do pedido.")
    public ResponseEntity<Pedido> aplicarDescontoFidelidade(
            @PathVariable("id") Long id,
            @RequestParam("clienteId") Long clienteId) {

        return ResponseEntity.ok(
                pedidoService.aplicarDescontoFidelidade(id, clienteId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar pedido",
            description = "ADMIN e GERENTE podem cancelar pedidos. CLIENTE so pode cancelar pedido proprio ainda nao processado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido cancelado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<Void> cancelarPedido(
            @Parameter(description = "Identificador do pedido", example = "1") @PathVariable("id") Long id) {

        pedidoService.cancelarPedido(id);

        return ResponseEntity.noContent().build();
    }
}

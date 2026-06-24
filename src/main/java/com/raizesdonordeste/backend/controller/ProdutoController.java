package com.raizesdonordeste.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.backend.dto.PageResponseDTO;
import com.raizesdonordeste.backend.dto.ProdutoDTO;
import com.raizesdonordeste.backend.entity.Produto;
import com.raizesdonordeste.backend.service.ProdutoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Produtos", description = "Cadastro e consulta de produtos")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class ProdutoController {

        private final ProdutoService produtoService;

        @PostMapping
        @Operation(summary = "Cadastrar produto", description = "Cria um produto no catálogo global.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Produto criado"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão"),
                        @ApiResponse(responseCode = "422", description = "Request inválido")
        })
        public ResponseEntity<Produto> criarProduto(
                        @Valid @RequestBody ProdutoDTO dto) {

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(produtoService.criarProduto(dto));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Consultar produto", description = "Retorna um produto pelo ID.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Produto encontrado"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão"),
                        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
        })
        public ResponseEntity<Produto> buscarProduto(
                        @Parameter(description = "Identificador do produto", example = "1")
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                produtoService.buscarProduto(id));
        }

        @GetMapping
        @Operation(summary = "Listar produtos", description = "Retorna todos os produtos cadastrados.")
        @ApiResponses({
                        @ApiResponse(
                                        responseCode = "200",
                                        description = "Resposta paginada de produtos",
                                        content = @io.swagger.v3.oas.annotations.media.Content(
                                                        schema = @Schema(implementation = PageResponseDTO.class))),
                        @ApiResponse(responseCode = "422", description = "Paginacao ou ordenacao invalida"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão")
        })
        public ResponseEntity<PageResponseDTO<Produto>> listarProdutos(
                        @Parameter(description = "Numero da pagina, iniciado em zero", example = "0")
                        @Min(value = 0, message = "page deve ser maior ou igual a zero")
                        @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Quantidade de registros por pagina, entre 1 e 100", example = "10")
                        @Min(value = 1, message = "limit deve ser maior ou igual a um")
                        @Max(value = 100, message = "limit deve ser menor ou igual a cem")
                        @RequestParam(defaultValue = "10") int limit,
                        @Parameter(
                                        description = "Campo usado para ordenacao",
                                        example = "nome",
                                        schema = @Schema(allowableValues = {"id", "nome", "preco"}))
                        @RequestParam(defaultValue = "nome") String sort,
                        @Parameter(
                                        description = "Direcao da ordenacao",
                                        example = "asc",
                                        schema = @Schema(allowableValues = {"asc", "desc"}))
                        @Pattern(regexp = "(?i)asc|desc", message = "direction deve ser asc ou desc")
                        @RequestParam(defaultValue = "asc") String direction) {

                return ResponseEntity.ok(
                                produtoService.listarProdutos(page, limit, sort, direction));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Atualizar produto",
                        description = "Substitui os dados editaveis do produto identificado.")
        public ResponseEntity<Produto> atualizarProduto(
                        @PathVariable Long id,
                        @Valid @RequestBody ProdutoDTO dto) {

                return ResponseEntity.ok(
                                produtoService.atualizarProduto(id, dto));
        }

        @PutMapping("/{id}/desativar")
        @Operation(summary = "Desativar produto",
                        description = "Marca o produto como indisponivel no catalogo global.")
        public ResponseEntity<Produto> desativarProduto(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                produtoService.desativarProduto(id));
        }
}

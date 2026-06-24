package com.raizesdonordeste.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.backend.dto.UnidadeDTO;
import com.raizesdonordeste.backend.entity.Unidade;
import com.raizesdonordeste.backend.service.UnidadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/unidades")
@RequiredArgsConstructor
@Tag(name = "Unidades", description = "Cadastro e consulta das unidades da rede")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class UnidadeController {

    private final UnidadeService unidadeService;

    @PostMapping
    @Operation(summary = "Cadastrar unidade",
            description = "Cria uma unidade da rede com localizacao, horarios e canais aceitos.")
    public ResponseEntity<Unidade> criarUnidade(
            @Valid @RequestBody UnidadeDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(unidadeService.criarUnidade(dto));
    }

    @GetMapping
    @Operation(summary = "Listar unidades", description = "Retorna todas as unidades cadastradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidades retornadas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<List<Unidade>> listarUnidades() {

        return ResponseEntity.ok(unidadeService.listarUnidades());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar unidade", description = "Retorna uma unidade pelo ID.")
    public ResponseEntity<Unidade> buscarUnidade(
            @PathVariable Long id) {

        return ResponseEntity.ok(unidadeService.buscarUnidade(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar unidade",
            description = "Substitui os dados editaveis da unidade identificada.")
    public ResponseEntity<Unidade> atualizarUnidade(
            @PathVariable Long id,
            @Valid @RequestBody UnidadeDTO dto) {

        return ResponseEntity.ok(unidadeService.atualizarUnidade(id, dto));
    }
}

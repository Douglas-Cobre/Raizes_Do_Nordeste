package com.raizesdonordeste.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.backend.dto.FuncionarioDTO;
import com.raizesdonordeste.backend.entity.Funcionario;
import com.raizesdonordeste.backend.service.FuncionarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/funcionarios")
@RequiredArgsConstructor
@Tag(name = "Funcionarios", description = "Cadastro, consulta e desativacao de funcionarios")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    @PostMapping
    @Operation(summary = "Cadastrar funcionario",
            description = "Cria um funcionario vinculado a uma unidade. A rota exige perfil ADMIN.")
    public ResponseEntity<Funcionario> criarFuncionario(
            @Valid @RequestBody FuncionarioDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(funcionarioService.criarFuncionario(dto));
    }

    @GetMapping
    @Operation(summary = "Listar funcionarios", description = "Retorna todos os funcionarios cadastrados.")
    public ResponseEntity<List<Funcionario>> listarFuncionarios() {

        return ResponseEntity.ok(funcionarioService.listarFuncionarios());
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar funcionarios ativos", description = "Retorna apenas funcionarios ativos.")
    public ResponseEntity<List<Funcionario>> listarFuncionariosAtivos() {

        return ResponseEntity.ok(funcionarioService.listarFuncionariosAtivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar funcionario", description = "Retorna um funcionario pelo ID.")
    public ResponseEntity<Funcionario> buscarFuncionario(
            @Parameter(description = "Identificador do funcionario", example = "1") @PathVariable Long id) {

        return ResponseEntity.ok(funcionarioService.buscarFuncionario(id));
    }

    @GetMapping("/unidade")
    @Operation(summary = "Listar funcionarios por unidade",
            description = "Retorna todos os funcionarios vinculados a uma unidade.")
    public ResponseEntity<List<Funcionario>> listarPorUnidade(
            @Parameter(description = "Identificador da unidade", example = "1") @RequestParam Long unidadeId) {

        return ResponseEntity.ok(
                funcionarioService.listarFuncionariosPorUnidade(unidadeId));
    }

    @GetMapping("/unidade/ativos")
    @Operation(summary = "Listar funcionarios ativos por unidade",
            description = "Retorna os funcionarios ativos vinculados a uma unidade.")
    public ResponseEntity<List<Funcionario>> listarAtivosPorUnidade(
            @RequestParam Long unidadeId) {

        return ResponseEntity.ok(
                funcionarioService.listarFuncionariosAtivosPorUnidade(unidadeId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar funcionario",
            description = "Substitui os dados editaveis do funcionario identificado.")
    public ResponseEntity<Funcionario> atualizarFuncionario(
            @PathVariable Long id,
            @Valid @RequestBody FuncionarioDTO dto) {

        return ResponseEntity.ok(
                funcionarioService.atualizarFuncionario(id, dto));
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar funcionario",
            description = "Marca o funcionario como inativo.")
    public ResponseEntity<Funcionario> desativarFuncionario(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                funcionarioService.desativarFuncionario(id));
    }
}

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
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.backend.service.ClienteService;
import com.raizesdonordeste.backend.service.AuthorizationService;
import com.raizesdonordeste.backend.dto.AuthResponseDTO;
import com.raizesdonordeste.backend.dto.ClienteDTO;
import com.raizesdonordeste.backend.dto.LoginDTO;
import com.raizesdonordeste.backend.entity.Cliente;
import com.raizesdonordeste.backend.entity.Pedido;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Cadastro e consulta de clientes")
public class ClienteController {

        private final ClienteService clienteService;
        private final AuthorizationService authorizationService;

        @PostMapping
        @Operation(summary = "Cadastrar cliente", description = "Cria uma conta de cliente. Este endpoint é público.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Cliente criado"),
                        @ApiResponse(responseCode = "409", description = "E-mail já cadastrado"),
                        @ApiResponse(responseCode = "422", description = "Request inválido")
        })
        public ResponseEntity<Cliente> criarCliente(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                        required = true,
                                        content = @Content(schema = @Schema(implementation = ClienteDTO.class),
                                                        examples = @ExampleObject(name = "Cadastro de cliente",
                                                                        value = """
                                                                                {
                                                                                  "nome": "Maria Silva",
                                                                                  "email": "maria@example.com",
                                                                                  "senha": "Cliente@123",
                                                                                  "telefone": "(81) 99999-9999",
                                                                                  "consentimentoLgpd": true
                                                                                }
                                                                                """)))
                        @Valid @RequestBody ClienteDTO dto) {

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(clienteService.criarCliente(dto));
        }

        @GetMapping("/{id}")
        @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
        @Operation(summary = "Consultar cliente", description = "Retorna o cliente identificado pelo ID.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "403", description = "Sem permissão"),
                        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
        })
        public ResponseEntity<Cliente> buscarCliente(
                        @Parameter(description = "Identificador do cliente", example = "1")
                        @PathVariable Long id) {

                authorizationService.validarAcessoAoCliente(id);
                return ResponseEntity.ok(
                                clienteService.buscarCliente(id));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Atualizar cliente",
                        description = "Substitui os dados cadastrais do cliente identificado.")
        @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<Cliente> atualizarCliente(
                        @PathVariable Long id,
                        @Valid @RequestBody ClienteDTO dto) {

                authorizationService.validarAcessoAoCliente(id);
                return ResponseEntity.ok(
                                clienteService.atualizarCliente(id, dto));
        }

        @GetMapping("/{id}/pedidos")
        @Operation(summary = "Consultar historico de pedidos",
                        description = "Retorna os pedidos associados ao cliente informado.")
        @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<List<Pedido>> buscarHistoricoPedidos(
                        @PathVariable Long id) {

                authorizationService.validarAcessoAoCliente(id);
                return ResponseEntity.ok(
                                clienteService.buscarHistoricoPedidos(id));
        }

        @PatchMapping("/{id}/consentimento")
        @Operation(summary = "Registrar consentimento LGPD",
                        description = "Registra o consentimento e sua data no cadastro do cliente.")
        @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<Void> registrarConsentimento(
                        @PathVariable Long id) {

                authorizationService.validarAcessoAoCliente(id);
                clienteService.registrarConsentimentoLgpd(id);

                return ResponseEntity.noContent().build();
        }

        @PostMapping("/login")
        @Operation(summary = "Autenticar cliente",
                        description = "Endpoint publico legado de login de cliente; retorna o mesmo formato JWT da autenticacao geral.")
        public ResponseEntity<AuthResponseDTO> login(
                        @Valid @RequestBody LoginDTO dto) {

                return ResponseEntity.ok(
                                clienteService.login(dto));
        }

}

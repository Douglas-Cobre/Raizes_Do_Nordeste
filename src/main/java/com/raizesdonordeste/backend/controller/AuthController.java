package com.raizesdonordeste.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.backend.dto.AuthResponseDTO;
import com.raizesdonordeste.backend.dto.LoginDTO;
import com.raizesdonordeste.backend.exception.ApiErrorResponse;
import com.raizesdonordeste.backend.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacao", description = "Emissao de token JWT para clientes e funcionarios")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario",
            description = "Valida e-mail e senha de cliente ou funcionario e retorna um token JWT com validade de oito horas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticacao realizada",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais invalidas",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Dados de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<AuthResponseDTO> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Credenciais do usuario",
                    content = @Content(schema = @Schema(implementation = LoginDTO.class),
                            examples = @ExampleObject(name = "Login de administrador",
                                    value = """
                                            {
                                              "email": "admin@raizesdonordeste.com",
                                              "senha": "Admin@123"
                                            }
                                            """)))
            @Valid @RequestBody LoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}

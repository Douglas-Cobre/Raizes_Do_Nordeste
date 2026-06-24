package com.raizesdonordeste.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credenciais para autenticacao")
public class LoginDTO {

    @NotBlank(message = "email é obrigatório.")
    @Email(message = "email deve ser válido.")
    @Schema(description = "E-mail cadastrado", example = "admin@raizesdonordeste.com")
    private String email;

    @NotBlank(message = "senha é obrigatória.")
    @Schema(description = "Senha do usuário", example = "Admin@123", minLength = 8)
    private String senha;
}

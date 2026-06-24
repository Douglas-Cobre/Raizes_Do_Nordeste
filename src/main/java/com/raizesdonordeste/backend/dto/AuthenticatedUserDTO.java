package com.raizesdonordeste.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo do usuario autenticado")
public record AuthenticatedUserDTO(
        @Schema(description = "Identificador do usuario", example = "1")
        Long id,
        @Schema(description = "Nome do usuario", example = "Administrador")
        String nome,
        @Schema(description = "Perfil usado na autorizacao", example = "ADMIN")
        String perfil) {
}

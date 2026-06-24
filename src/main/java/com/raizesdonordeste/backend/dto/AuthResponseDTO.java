package com.raizesdonordeste.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token JWT e dados resumidos do usuario autenticado")
public record AuthResponseDTO(
        @Schema(description = "Token JWT de acesso", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType,
        @Schema(description = "Validade do token em segundos", example = "28800")
        long expiresIn,
        @Schema(description = "Usuario autenticado")
        AuthenticatedUserDTO user) {
}

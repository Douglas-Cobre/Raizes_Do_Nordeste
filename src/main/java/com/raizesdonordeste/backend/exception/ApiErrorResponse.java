package com.raizesdonordeste.backend.exception;

import java.time.OffsetDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Formato padronizado das respostas de erro da API")
public record ApiErrorResponse(
        @Schema(description = "Codigo identificador do erro", example = "ESTOQUE_INSUFICIENTE")
        String error,
        @Schema(description = "Mensagem legivel sobre o erro", example = "Estoque insuficiente")
        String message,
        @Schema(description = "Detalhes dos campos invalidos; vazio em erros sem detalhe de campo")
        List<FieldErrorDetail> details,
        @Schema(description = "Data e hora do erro", example = "2026-06-21T10:00:00-03:00")
        OffsetDateTime timestamp,
        @Schema(description = "Rota que gerou o erro", example = "/pedidos")
        String path,
        @Schema(description = "Identificador opcional recebido no cabecalho X-Request-Id",
                example = "req-123", nullable = true)
        String requestId) {
}

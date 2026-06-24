package com.raizesdonordeste.backend.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalhe de um campo rejeitado pela validacao")
public record FieldErrorDetail(
        @Schema(description = "Nome do campo", example = "email")
        String field,
        @Schema(description = "Motivo da rejeicao", example = "email deve ser valido.")
        String issue) {
}

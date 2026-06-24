package com.raizesdonordeste.backend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Situacao do pagamento",
        allowableValues = {"PENDENTE", "APROVADO", "RECUSADO", "CANCELADO"})
public enum StatusPagamento {
    PENDENTE,
    APROVADO,
    RECUSADO,
    CANCELADO
}

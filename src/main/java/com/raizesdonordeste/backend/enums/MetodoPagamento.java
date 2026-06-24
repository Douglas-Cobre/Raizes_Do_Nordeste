package com.raizesdonordeste.backend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Metodo usado no pagamento",
        allowableValues = {"DINHEIRO", "CARTAO_CREDITO", "CARTAO_DEBITO", "PIX",
                "VALE_ALIMENTACAO", "VALE_REFEICAO", "MOCK"})
public enum MetodoPagamento {

    DINHEIRO,
    CARTAO_CREDITO,
    CARTAO_DEBITO,
    PIX,
    VALE_ALIMENTACAO,
    VALE_REFEICAO,
    MOCK

}

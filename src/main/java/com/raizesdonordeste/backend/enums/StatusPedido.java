package com.raizesdonordeste.backend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Etapa atual do pedido",
        allowableValues = {"CRIADO", "AGUARDANDO_PAGAMENTO", "PAGO", "EM_PREPARO",
                "PRONTO", "FINALIZADO", "CANCELADO", "ERRO_PAGAMENTO"})
public enum StatusPedido {
    CRIADO,
    AGUARDANDO_PAGAMENTO,
    PAGO,
    EM_PREPARO,
    PRONTO,
    FINALIZADO,
    CANCELADO,
    ERRO_PAGAMENTO
}

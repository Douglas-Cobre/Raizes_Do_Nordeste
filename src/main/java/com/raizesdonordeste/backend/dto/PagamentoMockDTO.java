package com.raizesdonordeste.backend.dto;

import com.raizesdonordeste.backend.enums.MetodoPagamento;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Dados para simular aprovacao ou recusa de pagamento")
public class PagamentoMockDTO {

    @NotNull(message = "pedidoId e obrigatorio.")
    @Schema(description = "Identificador do pedido", example = "1")
    private Long pedidoId;

    @NotNull(message = "metodoPagamento e obrigatorio.")
    @Schema(description = "Método usado na simulação", example = "MOCK",
            allowableValues = {"DINHEIRO", "CARTAO_CREDITO", "CARTAO_DEBITO", "PIX",
                    "VALE_ALIMENTACAO", "VALE_REFEICAO", "MOCK"})
    private MetodoPagamento metodoPagamento;

    @NotNull(message = "aprovado e obrigatorio.")
    @Schema(description = "Define se a simulação será aprovada", example = "true")
    private Boolean aprovado;
}

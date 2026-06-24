package com.raizesdonordeste.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Ajuste absoluto do saldo de estoque")
public class AjusteEstoqueDTO {

    @NotNull(message = "produtoId é obrigatório.")
    @Schema(description = "Identificador do produto", example = "1")
    private Long produtoId;

    @NotNull(message = "unidadeId é obrigatório.")
    @Schema(description = "Identificador da unidade", example = "1")
    private Long unidadeId;

    @NotNull(message = "novoSaldo é obrigatório.")
    @Schema(description = "Novo saldo absoluto", example = "25", minimum = "0")
    private Integer novoSaldo;

    @Schema(description = "Justificativa do ajuste", example = "Conferência de inventário")
    private String motivo;
}

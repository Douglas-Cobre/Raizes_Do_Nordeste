package com.raizesdonordeste.backend.dto;

import com.raizesdonordeste.backend.entity.Produto;
import com.raizesdonordeste.backend.entity.Unidade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Schema(description = "Dados para cadastro de saldo de estoque")
public class EstoqueDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "quantidade é obrigatória.")
    @Min(value = 0, message = "quantidade deve ser maior ou igual a zero.")
    @Schema(description = "Saldo inicial", example = "25", minimum = "0")
    private Integer quantidade;

    @Schema(description = "Limite configurado para alerta", example = "10")
    private Integer estoqueMinimo;

    @NotNull(message = "produto é obrigatório.")
    @Schema(description = "Produto associado; informar ao menos seu id")
    private Produto produto;

    @NotNull(message = "unidade é obrigatória.")
    @Schema(description = "Unidade associada; informar ao menos seu id")
    private Unidade unidade;
}

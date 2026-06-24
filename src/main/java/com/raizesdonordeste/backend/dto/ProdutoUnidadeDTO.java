package com.raizesdonordeste.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProdutoUnidadeDTO {

    private Long id;

    @NotNull(message = "produtoId é obrigatório.")
    private Long produtoId;

    @NotNull(message = "unidadeId é obrigatório.")
    private Long unidadeId;

    @NotNull(message = "preco é obrigatório.")
    private BigDecimal preco;

    @NotNull(message = "disponivel é obrigatório.")
    private Boolean disponivel;

    private String observacaoRegional;

    private LocalDate dataInicioDisponibilidade;

    private LocalDate dataFimDisponibilidade;
}

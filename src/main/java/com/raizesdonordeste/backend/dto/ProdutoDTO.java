package com.raizesdonordeste.backend.dto;

import java.util.List;

import com.raizesdonordeste.backend.entity.Estoque;
import com.raizesdonordeste.backend.entity.ProdutoUnidade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProdutoDTO {

    private Long id;

    @NotBlank(message = "nome é obrigatório.")
    @Schema(description = "Nome do produto", example = "Baião de Dois")
    private String nome;

    private String descricao;

    // private Boolean sazonal;

    private List<ProdutoUnidade> produtosUnidade;

    private List<Estoque> estoques;

    @NotNull(message = "disponivel é obrigatório.")
    @Schema(description = "Indica disponibilidade global", example = "true")
    private Boolean disponivel;

    @NotBlank(message = "categoria é obrigatória.")
    @Schema(description = "Categoria do produto", example = "PRATO_PRINCIPAL")
    private String categoria;

    @NotNull(message = "preco é obrigatório.")
    @Schema(description = "Preço padrão do produto", example = "28.90")
    private Double preco;

}

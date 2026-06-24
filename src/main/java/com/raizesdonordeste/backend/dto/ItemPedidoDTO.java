package com.raizesdonordeste.backend.dto;

import com.raizesdonordeste.backend.entity.Produto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Item solicitado em um pedido")
public class ItemPedidoDTO {

    @Schema(description = "Identificador do produto", example = "1")
    private Long produtoId;

    @Schema(description = "Forma legada de informar o produto; prefira produtoId",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    private Produto produto;

    @NotNull(message = "quantidade e obrigatoria.")
    @Min(value = 1, message = "quantidade deve ser maior que zero.")
    @Schema(description = "Quantidade solicitada", example = "2", minimum = "1")
    private Integer quantidade;

    public Long produtoIdResolvido() {
        if (produtoId != null) {
            return produtoId;
        }

        return produto != null ? produto.getId() : null;
    }
}

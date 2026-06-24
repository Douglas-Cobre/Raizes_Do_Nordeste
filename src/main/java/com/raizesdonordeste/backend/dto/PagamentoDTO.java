package com.raizesdonordeste.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.raizesdonordeste.backend.entity.Pedido;
import com.raizesdonordeste.backend.enums.MetodoPagamento;
import com.raizesdonordeste.backend.enums.StatusPagamento;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Dados para criacao e representacao de pagamento")
public class PagamentoDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY,
            allowableValues = {"PENDENTE", "APROVADO", "RECUSADO", "CANCELADO"})
    private StatusPagamento status;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String codigoTransacao;

    @NotNull(message = "valor e obrigatorio.")
    @Schema(description = "Valor do pagamento", example = "57.80")
    private BigDecimal valor;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataPagamento;

    @Schema(description = "Identificador do pedido", example = "1")
    private Long pedidoId;

    @Schema(description = "Forma legada de informar o pedido; prefira pedidoId",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    private Pedido pedido;

    @NotNull(message = "metodoPagamento e obrigatorio.")
    @Schema(description = "Metodo de pagamento", example = "PIX",
            allowableValues = {"DINHEIRO", "CARTAO_CREDITO", "CARTAO_DEBITO", "PIX",
                    "VALE_ALIMENTACAO", "VALE_REFEICAO", "MOCK"})
    private MetodoPagamento metodoPagamento;

    public Long pedidoIdResolvido() {
        if (pedidoId != null) {
            return pedidoId;
        }

        return pedido != null ? pedido.getId() : null;
    }
}

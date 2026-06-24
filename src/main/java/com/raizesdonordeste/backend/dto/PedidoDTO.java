package com.raizesdonordeste.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raizesdonordeste.backend.entity.Cliente;
import com.raizesdonordeste.backend.entity.Pagamento;
import com.raizesdonordeste.backend.entity.Unidade;
import com.raizesdonordeste.backend.enums.CanalPedido;
import com.raizesdonordeste.backend.enums.StatusPedido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Dados para criacao e representacao de um pedido")
public class PedidoDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY,
            allowableValues = {"CRIADO", "AGUARDANDO_PAGAMENTO", "PAGO", "EM_PREPARO",
                    "PRONTO", "FINALIZADO", "CANCELADO", "ERRO_PAGAMENTO"})
    private StatusPedido status;

    @NotNull(message = "canalPedido e obrigatorio.")
    @JsonProperty("canalPedido")
    @Schema(description = "Canal de origem do pedido", example = "APP",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"APP", "TOTEM", "BALCAO", "PICKUP", "WEB"})
    private CanalPedido canalPedido;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "57.80")
    private BigDecimal total;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "0.00")
    private BigDecimal desconto;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataCriacao;

    @Schema(description = "Identificador do cliente", example = "1")
    private Long clienteId;

    @Schema(description = "Forma legada de informar o cliente; prefira clienteId",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    private Cliente cliente;

    @Schema(description = "Identificador da unidade", example = "1")
    private Long unidadeId;

    @Schema(description = "Forma legada de informar a unidade; prefira unidadeId",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    private Unidade unidade;

    @Valid
    @NotEmpty(message = "itens e obrigatorio.")
    @Schema(description = "Itens solicitados")
    private List<ItemPedidoDTO> itens;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Pagamento pagamento;

    public Long clienteIdResolvido() {
        if (clienteId != null) {
            return clienteId;
        }

        return cliente != null ? cliente.getId() : null;
    }

    public Long unidadeIdResolvido() {
        if (unidadeId != null) {
            return unidadeId;
        }

        return unidade != null ? unidade.getId() : null;
    }
}

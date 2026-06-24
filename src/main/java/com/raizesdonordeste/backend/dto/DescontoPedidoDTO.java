package com.raizesdonordeste.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DescontoPedidoDTO {

    @NotNull(message = "valorDesconto é obrigatório.")
    private BigDecimal valorDesconto;

    private String motivo;
}

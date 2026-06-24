package com.raizesdonordeste.backend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Canal de origem do pedido",
        allowableValues = {"APP", "TOTEM", "BALCAO", "PICKUP", "WEB"})
public enum CanalPedido {
    APP,
    TOTEM,
    BALCAO,
    PICKUP,
    WEB
}

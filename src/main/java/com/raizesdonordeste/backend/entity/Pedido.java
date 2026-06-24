package com.raizesdonordeste.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raizesdonordeste.backend.enums.CanalPedido;
import com.raizesdonordeste.backend.enums.StatusPedido;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    @Enumerated(EnumType.STRING)
    @JsonProperty("canalPedido")
    private CanalPedido canalPedido;

    private BigDecimal total;

    private BigDecimal desconto;

    private LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "unidade_id")
    private Unidade unidade;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens;

    @OneToOne(mappedBy = "pedido")
    private Pagamento pagamento;
}

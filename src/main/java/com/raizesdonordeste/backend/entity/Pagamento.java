package com.raizesdonordeste.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.raizesdonordeste.backend.enums.MetodoPagamento;
import com.raizesdonordeste.backend.enums.StatusPagamento;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Data
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private StatusPagamento status;

    private String codigoTransacao;

    private BigDecimal valor;

    private LocalDateTime dataPagamento;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodoPagamento;
}

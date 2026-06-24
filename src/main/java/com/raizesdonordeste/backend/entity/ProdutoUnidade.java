package com.raizesdonordeste.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Data
public class ProdutoUnidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal preco;

    private Boolean disponivel;

    private String observacaoRegional;

    private LocalDate dataInicioDisponibilidade;

    private LocalDate dataFimDisponibilidade;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "unidade_id")
    private Unidade unidade;
}
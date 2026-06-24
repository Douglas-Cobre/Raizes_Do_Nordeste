package com.raizesdonordeste.backend.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Data
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String descricao;

    private Boolean disponivel;

    private String categoria;

    private Double preco;

    @JsonIgnore
    @OneToMany(mappedBy = "produto")
    private List<ProdutoUnidade> produtosUnidade;

    @JsonIgnore
    @OneToMany(mappedBy = "produto")
    private List<Estoque> estoques;
}

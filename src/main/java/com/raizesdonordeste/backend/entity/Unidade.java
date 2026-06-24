package com.raizesdonordeste.backend.entity;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Data
public class Unidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String cidade;

    private String estado;

    private Boolean ativa;

    private LocalTime horarioAbertura;

    private LocalTime horarioFechamento;

    private Boolean aceitaApp;

    private Boolean aceitaTotem;

    private Boolean aceitaBalcao;

    private Boolean aceitaPickup;

    private Boolean cozinhaCompleta;

    @JsonIgnore
    @OneToMany(mappedBy = "unidade")
    private List<Pedido> pedidos;

    @JsonIgnore
    @OneToMany(mappedBy = "unidade")
    private List<Funcionario> funcionarios;

    @JsonIgnore
    @OneToMany(mappedBy = "unidade")
    private List<Estoque> estoques;

    @JsonIgnore
    @OneToMany(mappedBy = "unidade")
    private List<ProdutoUnidade> produtosUnidade;
}

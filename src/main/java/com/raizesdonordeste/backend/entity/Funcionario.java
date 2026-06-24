package com.raizesdonordeste.backend.entity;

import com.raizesdonordeste.backend.enums.CargoFuncionario;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Data
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @Enumerated(EnumType.STRING)
    private CargoFuncionario cargo;

    private Boolean ativo;

    @ManyToOne
    @JoinColumn(name = "unidade_id")
    private Unidade unidade;
}

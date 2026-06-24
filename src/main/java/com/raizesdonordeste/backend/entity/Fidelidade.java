package com.raizesdonordeste.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Data
public class Fidelidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer pontos;

    private String nivel;

    @OneToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
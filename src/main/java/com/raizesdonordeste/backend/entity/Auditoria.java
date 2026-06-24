package com.raizesdonordeste.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Data
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String acao;

    private String usuario;

    private String entidade;

    private LocalDateTime dataHora;

    private String valorAnterior;

    private String valorNovo;
}
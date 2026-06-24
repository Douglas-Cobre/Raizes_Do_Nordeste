package com.raizesdonordeste.backend.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UnidadeDTO {

    private Long id;

    @NotBlank(message = "nome é obrigatório.")
    private String nome;

    @NotBlank(message = "cidade é obrigatória.")
    private String cidade;

    @NotBlank(message = "estado é obrigatório.")
    private String estado;

    @NotNull(message = "ativa é obrigatória.")
    private Boolean ativa;

    private LocalTime horarioAbertura;

    private LocalTime horarioFechamento;

    private Boolean aceitaApp;

    private Boolean aceitaTotem;

    private Boolean aceitaBalcao;

    private Boolean aceitaPickup;

    private Boolean cozinhaCompleta;
}

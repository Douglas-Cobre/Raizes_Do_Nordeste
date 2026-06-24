package com.raizesdonordeste.backend.dto;

import com.raizesdonordeste.backend.entity.Cliente;

import lombok.Data;

@Data
public class FidelidadeDTO {

    private Long id;

    private Integer pontos;

    private String nivel;

    private Cliente cliente;

}

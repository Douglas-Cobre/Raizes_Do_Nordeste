package com.raizesdonordeste.backend.dto;

import java.time.LocalTime;

import com.raizesdonordeste.backend.entity.Unidade;

public record UnidadeResponseDTO(
        Long id,
        String nome,
        String cidade,
        String estado,
        Boolean ativa,
        LocalTime horarioAbertura,
        LocalTime horarioFechamento,
        Boolean aceitaApp,
        Boolean aceitaTotem,
        Boolean aceitaBalcao,
        Boolean aceitaPickup,
        Boolean cozinhaCompleta) {

    public static UnidadeResponseDTO from(Unidade unidade) {
        if (unidade == null) {
            return null;
        }

        return new UnidadeResponseDTO(
                unidade.getId(),
                unidade.getNome(),
                unidade.getCidade(),
                unidade.getEstado(),
                unidade.getAtiva(),
                unidade.getHorarioAbertura(),
                unidade.getHorarioFechamento(),
                unidade.getAceitaApp(),
                unidade.getAceitaTotem(),
                unidade.getAceitaBalcao(),
                unidade.getAceitaPickup(),
                unidade.getCozinhaCompleta());
    }
}

package com.raizesdonordeste.backend.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.raizesdonordeste.backend.entity.Fidelidade;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.FidelidadeRepository;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Service
public class FidelidadeService {

    private final FidelidadeRepository fidelidadeRepository;

    public Fidelidade buscarFidelidadeCliente(Long clienteId) {

        return fidelidadeRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "FIDELIDADE_NAO_ENCONTRADA",
                        "Fidelidade não encontrada"));
    }

    public Fidelidade adicionarPontos(Long clienteId,
            Integer pontos) {

        validarPontos(pontos);
        Fidelidade fidelidade = buscarFidelidadeCliente(clienteId);

        fidelidade.setPontos(
                fidelidade.getPontos() + pontos);

        atualizarNivel(fidelidade);

        return fidelidadeRepository.save(fidelidade);
    }

    public Fidelidade removerPontos(Long clienteId,
            Integer pontos) {

        validarPontos(pontos);
        Fidelidade fidelidade = buscarFidelidadeCliente(clienteId);

        fidelidade.setPontos(
                fidelidade.getPontos() - pontos);

        if (fidelidade.getPontos() < 0) {
            fidelidade.setPontos(0);
        }

        atualizarNivel(fidelidade);

        return fidelidadeRepository.save(fidelidade);
    }

    public BigDecimal percentualDesconto(Fidelidade fidelidade) {
        if (fidelidade == null || fidelidade.getNivel() == null) {
            return BigDecimal.ZERO;
        }

        return switch (fidelidade.getNivel().toUpperCase()) {
            case "PRATA" -> BigDecimal.valueOf(5);
            case "OURO" -> BigDecimal.valueOf(10);
            default -> BigDecimal.ZERO;
        };
    }

    public BigDecimal percentualDescontoDoCliente(Long clienteId) {
        return percentualDesconto(buscarFidelidadeCliente(clienteId));
    }

    private void atualizarNivel(Fidelidade fidelidade) {

        Integer pontos = fidelidade.getPontos();

        if (pontos >= 200) {

            fidelidade.setNivel("OURO");

        } else if (pontos >= 100) {

            fidelidade.setNivel("PRATA");

        } else {

            fidelidade.setNivel("BRONZE");
        }
    }

    private void validarPontos(Integer pontos) {
        if (pontos == null || pontos <= 0) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "PONTOS_INVALIDOS",
                    "A quantidade de pontos deve ser maior que zero.");
        }
    }
}

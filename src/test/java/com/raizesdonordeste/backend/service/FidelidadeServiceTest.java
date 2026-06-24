package com.raizesdonordeste.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.raizesdonordeste.backend.entity.Fidelidade;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.FidelidadeRepository;

@ExtendWith(MockitoExtension.class)
class FidelidadeServiceTest {

    @Mock
    private FidelidadeRepository fidelidadeRepository;

    @InjectMocks
    private FidelidadeService fidelidadeService;

    @Test
    void deveBloquearPontosZeroOuNegativos() {
        assertThatThrownBy(() -> fidelidadeService.adicionarPontos(1L, 0))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo("PONTOS_INVALIDOS");

        assertThatThrownBy(() -> fidelidadeService.removerPontos(1L, -10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo("PONTOS_INVALIDOS");
    }

    @Test
    void deveLimitarSaldoAZeroAoRemoverMaisPontosQueODisponivel() {
        Fidelidade fidelidade = new Fidelidade();
        fidelidade.setPontos(20);
        fidelidade.setNivel("BRONZE");
        when(fidelidadeRepository.findByClienteId(1L)).thenReturn(Optional.of(fidelidade));
        when(fidelidadeRepository.save(fidelidade)).thenReturn(fidelidade);

        Fidelidade resultado = fidelidadeService.removerPontos(1L, 50);

        assertThat(resultado.getPontos()).isZero();
        assertThat(resultado.getNivel()).isEqualTo("BRONZE");
    }
}

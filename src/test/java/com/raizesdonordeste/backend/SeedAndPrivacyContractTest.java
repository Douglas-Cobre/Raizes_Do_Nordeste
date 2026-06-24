package com.raizesdonordeste.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raizesdonordeste.backend.entity.Cliente;
import com.raizesdonordeste.backend.entity.Funcionario;
import com.raizesdonordeste.backend.repository.ClienteRepository;
import com.raizesdonordeste.backend.repository.FidelidadeRepository;

@SpringBootTest
@ActiveProfiles("test")
class SeedAndPrivacyContractTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private FidelidadeRepository fidelidadeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void clienteSeedPossuiFidelidadeBronzeComSaldoZero() {
        Cliente cliente = clienteRepository.findByEmail("cliente.teste@raizesdonordeste.com")
                .orElseThrow();

        assertThat(fidelidadeRepository.findByClienteId(cliente.getId()))
                .get()
                .satisfies(fidelidade -> {
                    assertThat(fidelidade.getPontos()).isZero();
                    assertThat(fidelidade.getNivel()).isEqualTo("BRONZE");
                });
    }

    @Test
    void responsesNaoSerializamSenhaOuHash() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setSenha("$2a$hash-cliente");
        Funcionario funcionario = new Funcionario();
        funcionario.setSenha("$2a$hash-funcionario");

        assertThat(objectMapper.writeValueAsString(cliente)).doesNotContain("senha", "hash-cliente");
        assertThat(objectMapper.writeValueAsString(funcionario)).doesNotContain("senha", "hash-funcionario");
    }
}

package com.raizesdonordeste.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.raizesdonordeste.backend.entity.Cliente;
import com.raizesdonordeste.backend.entity.Fidelidade;
import com.raizesdonordeste.backend.repository.ClienteRepository;
import com.raizesdonordeste.backend.repository.FidelidadeRepository;
import com.raizesdonordeste.backend.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private FidelidadeRepository fidelidadeRepository;

    @Test
    void endpointProtegidoSemTokenRetorna401Padronizado() throws Exception {
        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("NAO_AUTENTICADO"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.path").value("/pedidos"));
    }

    @Test
    void tokenInvalidoRetorna401Padronizado() throws Exception {
        mockMvc.perform(get("/pedidos")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("NAO_AUTENTICADO"));
    }

    @Test
    void clienteNaoPodeAtualizarStatusDoPedido() throws Exception {
        String token = tokenClienteSeed();

        mockMvc.perform(patch("/pedidos/1/status")
                        .param("status", "EM_PREPARO")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("ACESSO_NEGADO"))
                .andExpect(jsonPath("$.path").value("/pedidos/1/status"));
    }

    @Test
    @Transactional
    void clientePodeConsultarProprioCadastroEFidelidade() throws Exception {
        Cliente cliente = clienteSeed();
        String token = tokenClienteSeed();

        mockMvc.perform(get("/clientes/{id}", cliente.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/fidelidade/{clienteId}", cliente.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/fidelidade/{clienteId}/desconto", cliente.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void clienteNaoPodeAcessarDadosDeOutroCliente() throws Exception {
        Cliente outroCliente = criarOutroCliente();
        String token = tokenClienteSeed();
        String bodyAtualizacao = """
                {
                  "nome": "Outro Cliente",
                  "email": "outro.cliente@teste.com",
                  "senha": "Cliente@123",
                  "telefone": "(81) 98888-7777",
                  "consentimentoLgpd": true
                }
                """;

        mockMvc.perform(get("/clientes/{id}", outroCliente.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACESSO_NEGADO"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/clientes/{id}", outroCliente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizacao)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACESSO_NEGADO"));

        mockMvc.perform(get("/clientes/{id}/pedidos", outroCliente.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACESSO_NEGADO"));

        mockMvc.perform(patch("/clientes/{id}/consentimento", outroCliente.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACESSO_NEGADO"));

        mockMvc.perform(get("/fidelidade/{clienteId}", outroCliente.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACESSO_NEGADO"));

        mockMvc.perform(get("/fidelidade/{clienteId}/desconto", outroCliente.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACESSO_NEGADO"));
    }

    @Test
    void clienteNaoPodeMovimentarPontos() throws Exception {
        String token = tokenClienteSeed();

        mockMvc.perform(patch("/fidelidade/1/adicionar")
                        .param("pontos", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACESSO_NEGADO"));

        mockMvc.perform(patch("/fidelidade/1/remover")
                        .param("pontos", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACESSO_NEGADO"));
    }

    private Cliente clienteSeed() {
        return clienteRepository.findByEmail("cliente.teste@raizesdonordeste.com")
                .orElseThrow();
    }

    private String tokenClienteSeed() {
        return jwtService.generateToken(
                "cliente.teste@raizesdonordeste.com",
                "CLIENTE",
                "Cliente Teste");
    }

    private Cliente criarOutroCliente() {
        Cliente cliente = new Cliente();
        cliente.setNome("Outro Cliente");
        cliente.setEmail("outro.cliente@teste.com");
        cliente.setSenha("hash-nao-utilizado");
        cliente.setConsentimentoLgpd(false);
        Cliente salvo = clienteRepository.save(cliente);

        Fidelidade fidelidade = new Fidelidade();
        fidelidade.setCliente(salvo);
        fidelidade.setPontos(0);
        fidelidade.setNivel("BRONZE");
        fidelidadeRepository.save(fidelidade);
        return salvo;
    }
}

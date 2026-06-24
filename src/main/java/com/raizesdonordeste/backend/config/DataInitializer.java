package com.raizesdonordeste.backend.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.raizesdonordeste.backend.entity.Cliente;
import com.raizesdonordeste.backend.entity.Estoque;
import com.raizesdonordeste.backend.entity.Funcionario;
import com.raizesdonordeste.backend.entity.Fidelidade;
import com.raizesdonordeste.backend.entity.Produto;
import com.raizesdonordeste.backend.entity.ProdutoUnidade;
import com.raizesdonordeste.backend.entity.Unidade;
import com.raizesdonordeste.backend.enums.CargoFuncionario;
import com.raizesdonordeste.backend.repository.ClienteRepository;
import com.raizesdonordeste.backend.repository.EstoqueRepository;
import com.raizesdonordeste.backend.repository.FuncionarioRepository;
import com.raizesdonordeste.backend.repository.FidelidadeRepository;
import com.raizesdonordeste.backend.repository.ProdutoRepository;
import com.raizesdonordeste.backend.repository.ProdutoUnidadeRepository;
import com.raizesdonordeste.backend.repository.UnidadeRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedDemoData(
            FuncionarioRepository funcionarioRepository,
            UnidadeRepository unidadeRepository,
            ProdutoRepository produtoRepository,
            ProdutoUnidadeRepository produtoUnidadeRepository,
            EstoqueRepository estoqueRepository,
            ClienteRepository clienteRepository,
            FidelidadeRepository fidelidadeRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin-email}") String adminEmail,
            @Value("${app.bootstrap.admin-password}") String adminPassword) {

        return args -> {
            Unidade unidade = unidadeRepository.findAll().stream()
                    .filter(item -> "Raizes Centro".equals(item.getNome()))
                    .findFirst()
                    .orElseGet(() -> {
                        Unidade novaUnidade = new Unidade();
                        novaUnidade.setNome("Raizes Centro");
                        novaUnidade.setCidade("Recife");
                        novaUnidade.setEstado("PE");
                        novaUnidade.setAtiva(true);
                        novaUnidade.setHorarioAbertura(LocalTime.of(10, 0));
                        novaUnidade.setHorarioFechamento(LocalTime.of(22, 0));
                        novaUnidade.setAceitaApp(true);
                        novaUnidade.setAceitaTotem(true);
                        novaUnidade.setAceitaBalcao(true);
                        novaUnidade.setAceitaPickup(true);
                        novaUnidade.setCozinhaCompleta(true);
                        return unidadeRepository.save(novaUnidade);
                    });

            Optional<Funcionario> existing = funcionarioRepository.findByEmail(adminEmail);
            if (existing.isEmpty()) {
                Funcionario admin = new Funcionario();
                admin.setNome("Administrador");
                admin.setEmail(adminEmail);
                admin.setSenha(passwordEncoder.encode(adminPassword));
                admin.setCargo(CargoFuncionario.ADMIN);
                admin.setAtivo(true);
                admin.setUnidade(unidade);

                funcionarioRepository.save(admin);
            }

            Cliente cliente = clienteRepository.findByEmail("cliente.teste@raizesdonordeste.com")
                    .orElseGet(() -> {
                        Cliente novoCliente = new Cliente();
                        novoCliente.setNome("Cliente Teste");
                        novoCliente.setEmail("cliente.teste@raizesdonordeste.com");
                        novoCliente.setSenha(passwordEncoder.encode("Cliente@123"));
                        novoCliente.setTelefone("(81) 99999-0000");
                        novoCliente.setConsentimentoLgpd(true);
                        novoCliente.setDataConsentimento(LocalDateTime.now());
                        return clienteRepository.save(novoCliente);
                    });
            fidelidadeRepository.findByClienteId(cliente.getId())
                    .orElseGet(() -> {
                        Fidelidade fidelidade = new Fidelidade();
                        fidelidade.setCliente(cliente);
                        fidelidade.setPontos(0);
                        fidelidade.setNivel("BRONZE");
                        return fidelidadeRepository.save(fidelidade);
                    });

            Produto baiao = produtoRepository.findAll().stream()
                    .filter(item -> "Baiao de Dois".equals(item.getNome()))
                    .findFirst()
                    .orElseGet(() -> criarProduto(produtoRepository, "Baiao de Dois",
                            "Arroz, feijao verde, queijo coalho e temperos nordestinos", "Prato principal", 28.90));

            Produto cuscuz = produtoRepository.findAll().stream()
                    .filter(item -> "Cuscuz Recheado".equals(item.getNome()))
                    .findFirst()
                    .orElseGet(() -> criarProduto(produtoRepository, "Cuscuz Recheado",
                            "Cuscuz de milho com carne de sol e queijo coalho", "Lanche", 18.50));

            vincularProdutoUnidade(produtoUnidadeRepository, baiao, unidade, new BigDecimal("28.90"));
            vincularProdutoUnidade(produtoUnidadeRepository, cuscuz, unidade, new BigDecimal("18.50"));

            criarEstoqueSeNecessario(estoqueRepository, baiao, unidade, 30, 5);
            criarEstoqueSeNecessario(estoqueRepository, cuscuz, unidade, 25, 5);
        };
    }

    private Produto criarProduto(
            ProdutoRepository produtoRepository,
            String nome,
            String descricao,
            String categoria,
            Double preco) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setCategoria(categoria);
        produto.setPreco(preco);
        produto.setDisponivel(true);
        return produtoRepository.save(produto);
    }

    private void vincularProdutoUnidade(
            ProdutoUnidadeRepository produtoUnidadeRepository,
            Produto produto,
            Unidade unidade,
            BigDecimal preco) {
        produtoUnidadeRepository.findByProdutoIdAndUnidadeId(produto.getId(), unidade.getId())
                .orElseGet(() -> {
                    ProdutoUnidade produtoUnidade = new ProdutoUnidade();
                    produtoUnidade.setProduto(produto);
                    produtoUnidade.setUnidade(unidade);
                    produtoUnidade.setPreco(preco);
                    produtoUnidade.setDisponivel(true);
                    produtoUnidade.setObservacaoRegional("Disponivel para demonstracao do MVP");
                    produtoUnidade.setDataInicioDisponibilidade(LocalDate.now());
                    return produtoUnidadeRepository.save(produtoUnidade);
                });
    }

    private void criarEstoqueSeNecessario(
            EstoqueRepository estoqueRepository,
            Produto produto,
            Unidade unidade,
            Integer quantidade,
            Integer estoqueMinimo) {
        estoqueRepository.findByProdutoIdAndUnidadeId(produto.getId(), unidade.getId())
                .orElseGet(() -> {
                    Estoque estoque = new Estoque();
                    estoque.setProduto(produto);
                    estoque.setUnidade(unidade);
                    estoque.setQuantidade(quantidade);
                    estoque.setEstoqueMinimo(estoqueMinimo);
                    return estoqueRepository.save(estoque);
                });
    }
}

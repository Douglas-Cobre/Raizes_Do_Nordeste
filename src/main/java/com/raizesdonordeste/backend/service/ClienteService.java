package com.raizesdonordeste.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.raizesdonordeste.backend.dto.ClienteDTO;
import com.raizesdonordeste.backend.dto.AuthResponseDTO;
import com.raizesdonordeste.backend.dto.LoginDTO;
import com.raizesdonordeste.backend.entity.Cliente;
import com.raizesdonordeste.backend.entity.Fidelidade;
import com.raizesdonordeste.backend.entity.Pedido;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.ClienteRepository;
import com.raizesdonordeste.backend.repository.FidelidadeRepository;
import com.raizesdonordeste.backend.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    private final PedidoRepository pedidoRepository;

    private final FidelidadeRepository fidelidadeRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final AuditoriaService auditoriaService;

    public Cliente criarCliente(ClienteDTO dto) {

        clienteRepository.findByEmail(dto.getEmail())
                .ifPresent(cliente -> {
                    throw new BusinessException(
                            HttpStatus.CONFLICT,
                            "EMAIL_JA_CADASTRADO",
                            "E-mail já cadastrado");
                });

        Cliente cliente = new Cliente();

        cliente.setNome(dto.getNome());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());

        cliente.setSenha(
                passwordEncoder.encode(dto.getSenha()));

        cliente.setConsentimentoLgpd(dto.getConsentimentoLgpd());

        cliente.setDataConsentimento(LocalDateTime.now());

        Cliente clienteSalvo = clienteRepository.save(cliente);

        Fidelidade fidelidade = new Fidelidade();

        fidelidade.setCliente(clienteSalvo);
        fidelidade.setPontos(0);
        fidelidade.setNivel("BRONZE");

        fidelidadeRepository.save(fidelidade);
        auditoriaService.registrar(
                "CRIAR_CLIENTE",
                "Cliente",
                null,
                resumoCliente(clienteSalvo));

        return clienteSalvo;
    }

    public Cliente buscarCliente(Long id) {

        return clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "CLIENTE_NAO_ENCONTRADO",
                        "Cliente não encontrado"));
    }

    public Cliente atualizarCliente(Long id,
            ClienteDTO dto) {

        Cliente cliente = buscarCliente(id);

        cliente.setNome(dto.getNome());
        cliente.setTelefone(dto.getTelefone());

        Cliente salvo = clienteRepository.save(cliente);
        auditoriaService.registrar(
                "ATUALIZAR_CLIENTE",
                "Cliente",
                null,
                resumoCliente(salvo));
        return salvo;
    }

    public List<Pedido> buscarHistoricoPedidos(Long clienteId) {

        return pedidoRepository.findByClienteId(clienteId);
    }

    public void registrarConsentimentoLgpd(Long clienteId) {

        Cliente cliente = buscarCliente(clienteId);

        cliente.setConsentimentoLgpd(true);

        cliente.setDataConsentimento(LocalDateTime.now());

        clienteRepository.save(cliente);
        auditoriaService.registrar(
                "REGISTRAR_CONSENTIMENTO_LGPD",
                "Cliente",
                null,
                resumoCliente(cliente));
    }

    public AuthResponseDTO login(LoginDTO dto) {
        return authService.login(dto);
    }

    private String resumoCliente(Cliente cliente) {
        return String.format(
                "{id:%s,nome:%s,email:%s,consentimentoLgpd:%s}",
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getConsentimentoLgpd());
    }

}

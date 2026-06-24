package com.raizesdonordeste.backend.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.raizesdonordeste.backend.entity.Cliente;
import com.raizesdonordeste.backend.entity.Pedido;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void clienteAcessaPedidoProprioMasNaoPedidoDeOutroCliente() {
        Cliente autenticado = cliente(1L);
        when(clienteRepository.findByEmail("cliente@teste.com"))
                .thenReturn(Optional.of(autenticado));
        autenticar("cliente@teste.com", "ROLE_CLIENTE");
        AuthorizationService service = new AuthorizationService(clienteRepository);

        Pedido proprio = new Pedido();
        proprio.setCliente(cliente(1L));
        Pedido alheio = new Pedido();
        alheio.setCliente(cliente(2L));

        assertThatCode(() -> service.validarAcessoAoPedido(proprio))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> service.validarAcessoAoPedido(alheio))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo("ACESSO_NEGADO");
    }

    @Test
    void gerentePodeAcessarPedidoDeQualquerCliente() {
        autenticar("gerente@teste.com", "ROLE_GERENTE");
        AuthorizationService service = new AuthorizationService(clienteRepository);
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente(2L));

        assertThatCode(() -> service.validarAcessoAoPedido(pedido))
                .doesNotThrowAnyException();
    }

    @Test
    void clienteAcessaSomenteOProprioCadastro() {
        Cliente autenticado = cliente(1L);
        when(clienteRepository.findByEmail("cliente@teste.com"))
                .thenReturn(Optional.of(autenticado));
        autenticar("cliente@teste.com", "ROLE_CLIENTE");
        AuthorizationService service = new AuthorizationService(clienteRepository);

        assertThatCode(() -> service.validarAcessoAoCliente(1L))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> service.validarAcessoAoCliente(2L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo("ACESSO_NEGADO");
    }

    @Test
    void adminEGerentePodemAcessarQualquerCliente() {
        AuthorizationService service = new AuthorizationService(clienteRepository);

        autenticar("admin@teste.com", "ROLE_ADMIN");
        assertThatCode(() -> service.validarAcessoAoCliente(2L))
                .doesNotThrowAnyException();

        autenticar("gerente@teste.com", "ROLE_GERENTE");
        assertThatCode(() -> service.validarAcessoAoCliente(2L))
                .doesNotThrowAnyException();
    }

    private void autenticar(String email, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority(role))));
    }

    private Cliente cliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        return cliente;
    }
}

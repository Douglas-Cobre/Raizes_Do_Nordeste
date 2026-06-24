package com.raizesdonordeste.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.raizesdonordeste.backend.entity.Cliente;
import com.raizesdonordeste.backend.entity.Pedido;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.ClienteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final ClienteRepository clienteRepository;

    public boolean isAdminOrGerente() {
        Authentication authentication = authentication();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_GERENTE".equals(authority.getAuthority()));
    }

    public boolean isCliente() {
        return authentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_CLIENTE".equals(authority.getAuthority()));
    }

    public Long clienteIdAutenticado() {
        String email = authentication().getName();
        return clienteRepository.findByEmail(email)
                .map(Cliente::getId)
                .orElseThrow(this::acessoNegado);
    }

    public void validarAcessoAoCliente(Long clienteId) {
        if (!isAdminOrGerente() && !clienteIdAutenticado().equals(clienteId)) {
            throw acessoNegado();
        }
    }

    public void validarAcessoAoPedido(Pedido pedido) {
        if (isAdminOrGerente()) {
            return;
        }

        Long donoPedido = pedido.getCliente() != null ? pedido.getCliente().getId() : null;
        if (donoPedido == null || !clienteIdAutenticado().equals(donoPedido)) {
            throw acessoNegado();
        }
    }

    private Authentication authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw acessoNegado();
        }
        return authentication;
    }

    private BusinessException acessoNegado() {
        return new BusinessException(
                HttpStatus.FORBIDDEN,
                "ACESSO_NEGADO",
                "O usuario autenticado nao possui permissao para esta operacao.");
    }
}

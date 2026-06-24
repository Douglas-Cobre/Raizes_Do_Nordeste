package com.raizesdonordeste.backend.service;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.raizesdonordeste.backend.entity.Auditoria;
import com.raizesdonordeste.backend.repository.AuditoriaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public void registrar(String acao, String entidade, String valorAnterior, String valorNovo) {
        log.info("Auditoria: acao={}, entidade={}, usuario={}", acao, entidade, obterUsuarioAtual());
        Auditoria auditoria = new Auditoria();
        auditoria.setAcao(acao);
        auditoria.setEntidade(entidade);
        auditoria.setValorAnterior(valorAnterior);
        auditoria.setValorNovo(valorNovo);
        auditoria.setDataHora(LocalDateTime.now());
        auditoria.setUsuario(obterUsuarioAtual());
        auditoriaRepository.save(auditoria);
    }

    private String obterUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "sistema";
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            return "sistema";
        }

        return authentication.getName();
    }
}

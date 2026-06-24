package com.raizesdonordeste.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.raizesdonordeste.backend.dto.FuncionarioDTO;
import com.raizesdonordeste.backend.entity.Funcionario;
import com.raizesdonordeste.backend.entity.Unidade;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.FuncionarioRepository;
import com.raizesdonordeste.backend.repository.UnidadeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    private final UnidadeRepository unidadeRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuditoriaService auditoriaService;

    public Funcionario criarFuncionario(FuncionarioDTO dto) {
        funcionarioRepository.findByEmail(dto.getEmail())
                .ifPresent(funcionario -> {
                    throw new BusinessException(
                            HttpStatus.CONFLICT,
                            "EMAIL_JA_CADASTRADO",
                            "E-mail já cadastrado");
                });

        Unidade unidade = buscarUnidadePorId(dto.getUnidadeId());

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(dto.getNome());
        funcionario.setEmail(dto.getEmail());
        funcionario.setSenha(passwordEncoder.encode(dto.getSenha()));
        funcionario.setCargo(dto.getCargo());
        funcionario.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        funcionario.setUnidade(unidade);

        Funcionario salvo = funcionarioRepository.save(funcionario);
        auditoriaService.registrar(
                "CRIAR_FUNCIONARIO",
                "Funcionario",
                null,
                resumoFuncionario(salvo));
        return salvo;
    }

    public Funcionario buscarFuncionario(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "FUNCIONARIO_NAO_ENCONTRADO",
                        "Funcionario não encontrado"));
    }

    public List<Funcionario> listarFuncionarios() {
        return funcionarioRepository.findAll();
    }

    public List<Funcionario> listarFuncionariosAtivos() {
        return funcionarioRepository.findByAtivoTrue();
    }

    public List<Funcionario> listarFuncionariosPorUnidade(Long unidadeId) {
        return funcionarioRepository.findByUnidadeId(unidadeId);
    }

    public List<Funcionario> listarFuncionariosAtivosPorUnidade(Long unidadeId) {
        return funcionarioRepository.findByUnidadeIdAndAtivoTrue(unidadeId);
    }

    public Funcionario atualizarFuncionario(Long id, FuncionarioDTO dto) {
        Funcionario funcionario = buscarFuncionario(id);
        Unidade unidade = buscarUnidadePorId(dto.getUnidadeId());

        funcionario.setNome(dto.getNome());
        funcionario.setEmail(dto.getEmail());
        funcionario.setSenha(passwordEncoder.encode(dto.getSenha()));
        funcionario.setCargo(dto.getCargo());
        funcionario.setAtivo(
                dto.getAtivo() != null ? dto.getAtivo() : funcionario.getAtivo());
        funcionario.setUnidade(unidade);

        Funcionario salvo = funcionarioRepository.save(funcionario);
        auditoriaService.registrar(
                "ATUALIZAR_FUNCIONARIO",
                "Funcionario",
                null,
                resumoFuncionario(salvo));
        return salvo;
    }

    public Funcionario desativarFuncionario(Long id) {
        Funcionario funcionario = buscarFuncionario(id);
        funcionario.setAtivo(false);

        Funcionario salvo = funcionarioRepository.save(funcionario);
        auditoriaService.registrar(
                "DESATIVAR_FUNCIONARIO",
                "Funcionario",
                null,
                resumoFuncionario(salvo));
        return salvo;
    }

    private Unidade buscarUnidadePorId(Long unidadeId) {
        if (unidadeId == null) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "UNIDADE_OBRIGATORIA",
                    "Unidade é obrigatória");
        }

        return unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "UNIDADE_NAO_ENCONTRADA",
                        "Unidade não encontrada"));
    }

    private String resumoFuncionario(Funcionario funcionario) {
        return String.format(
                "{id:%s,nome:%s,email:%s,cargo:%s,ativo:%s,unidadeId:%s}",
                funcionario.getId(),
                funcionario.getNome(),
                funcionario.getEmail(),
                funcionario.getCargo(),
                funcionario.getAtivo(),
                funcionario.getUnidade() != null ? funcionario.getUnidade().getId() : null);
    }
}

package com.raizesdonordeste.backend.service;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.raizesdonordeste.backend.dto.AuthResponseDTO;
import com.raizesdonordeste.backend.dto.AuthenticatedUserDTO;
import com.raizesdonordeste.backend.dto.LoginDTO;
import com.raizesdonordeste.backend.entity.Cliente;
import com.raizesdonordeste.backend.entity.Funcionario;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.ClienteRepository;
import com.raizesdonordeste.backend.repository.FuncionarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration TOKEN_TTL = Duration.ofHours(8);

    private final ClienteRepository clienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponseDTO login(LoginDTO dto) {
        Cliente cliente = clienteRepository.findByEmail(dto.getEmail())
                .filter(c -> passwordEncoder.matches(dto.getSenha(), c.getSenha()))
                .orElse(null);

        if (cliente != null) {
            String token = jwtService.generateToken(
                    cliente.getEmail(),
                    "CLIENTE",
                    cliente.getNome());

            return new AuthResponseDTO(
                    token,
                    "Bearer",
                    TOKEN_TTL.toSeconds(),
                    new AuthenticatedUserDTO(cliente.getId(), cliente.getNome(), "CLIENTE"));
        }

        Funcionario funcionario = funcionarioRepository.findByEmail(dto.getEmail())
                .filter(f -> passwordEncoder.matches(dto.getSenha(), f.getSenha()))
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.UNAUTHORIZED,
                        "CREDENCIAIS_INVALIDAS",
                        "E-mail ou senha inválidos."));

        String perfil = funcionario.getCargo().name();
        String token = jwtService.generateToken(
                funcionario.getEmail(),
                perfil,
                funcionario.getNome());

        return new AuthResponseDTO(
                token,
                "Bearer",
                TOKEN_TTL.toSeconds(),
                new AuthenticatedUserDTO(funcionario.getId(), funcionario.getNome(), perfil));
    }
}

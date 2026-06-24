package com.raizesdonordeste.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.backend.entity.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByEmail(String email);

    List<Funcionario> findByUnidadeId(Long unidadeId);

    List<Funcionario> findByAtivoTrue();

    List<Funcionario> findByUnidadeIdAndAtivoTrue(Long unidadeId);
}

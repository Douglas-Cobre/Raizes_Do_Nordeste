package com.raizesdonordeste.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.backend.entity.Fidelidade;

public interface FidelidadeRepository extends JpaRepository<Fidelidade, Long> {

    Optional<Fidelidade> findByClienteId(Long clienteId);
}

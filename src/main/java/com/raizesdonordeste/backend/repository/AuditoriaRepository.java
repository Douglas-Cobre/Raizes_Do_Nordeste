package com.raizesdonordeste.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.backend.entity.Auditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
}

package com.raizesdonordeste.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.raizesdonordeste.backend.dto.UnidadeDTO;
import com.raizesdonordeste.backend.entity.Unidade;
import com.raizesdonordeste.backend.exception.BusinessException;
import com.raizesdonordeste.backend.repository.UnidadeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnidadeService {

    private final UnidadeRepository unidadeRepository;

    public Unidade criarUnidade(UnidadeDTO dto) {

        Unidade unidade = new Unidade();

        unidade.setNome(dto.getNome());
        unidade.setCidade(dto.getCidade());
        unidade.setEstado(dto.getEstado());
        unidade.setAtiva(dto.getAtiva() != null ? dto.getAtiva() : true);
        unidade.setHorarioAbertura(dto.getHorarioAbertura());
        unidade.setHorarioFechamento(dto.getHorarioFechamento());
        unidade.setAceitaApp(dto.getAceitaApp() != null ? dto.getAceitaApp() : true);
        unidade.setAceitaTotem(dto.getAceitaTotem() != null ? dto.getAceitaTotem() : true);
        unidade.setAceitaBalcao(dto.getAceitaBalcao() != null ? dto.getAceitaBalcao() : true);
        unidade.setAceitaPickup(dto.getAceitaPickup() != null ? dto.getAceitaPickup() : true);
        unidade.setCozinhaCompleta(dto.getCozinhaCompleta() != null ? dto.getCozinhaCompleta() : true);

        return unidadeRepository.save(unidade);
    }

    public Unidade buscarUnidade(Long id) {

        return unidadeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "UNIDADE_NAO_ENCONTRADA",
                        "Unidade não encontrada"));
    }

    public List<Unidade> listarUnidades() {

        return unidadeRepository.findAll();
    }

    public Unidade atualizarUnidade(Long id, UnidadeDTO dto) {

        Unidade unidade = buscarUnidade(id);

        unidade.setNome(dto.getNome());
        unidade.setCidade(dto.getCidade());
        unidade.setEstado(dto.getEstado());
        unidade.setAtiva(dto.getAtiva());
        unidade.setHorarioAbertura(dto.getHorarioAbertura());
        unidade.setHorarioFechamento(dto.getHorarioFechamento());
        unidade.setAceitaApp(dto.getAceitaApp());
        unidade.setAceitaTotem(dto.getAceitaTotem());
        unidade.setAceitaBalcao(dto.getAceitaBalcao());
        unidade.setAceitaPickup(dto.getAceitaPickup());
        unidade.setCozinhaCompleta(dto.getCozinhaCompleta());

        return unidadeRepository.save(unidade);
    }

    public Unidade desativarUnidade(Long id) {

        Unidade unidade = buscarUnidade(id);
        unidade.setAtiva(false);

        return unidadeRepository.save(unidade);
    }
}

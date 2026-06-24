package com.raizesdonordeste.backend.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta paginada padronizada")
public record PageResponseDTO<T>(
        @Schema(description = "Registros da pagina atual")
        List<T> content,
        @Schema(description = "Numero da pagina atual, iniciado em zero", example = "0")
        int page,
        @Schema(description = "Quantidade maxima de registros por pagina", example = "10")
        int limit,
        @Schema(description = "Quantidade total de registros", example = "25")
        long totalElements,
        @Schema(description = "Quantidade total de paginas", example = "3")
        int totalPages,
        @Schema(description = "Indica se esta e a primeira pagina", example = "true")
        boolean first,
        @Schema(description = "Indica se esta e a ultima pagina", example = "false")
        boolean last) {

    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}

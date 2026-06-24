package com.raizesdonordeste.backend.dto;

import com.raizesdonordeste.backend.enums.CargoFuncionario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FuncionarioDTO {

    private Long id;

    @NotBlank(message = "nome é obrigatório.")
    private String nome;

    @NotBlank(message = "email é obrigatório.")
    @Email(message = "email deve ser válido.")
    private String email;

    @NotBlank(message = "senha é obrigatória.")
    @Size(min = 8, message = "senha deve ter ao menos 8 caracteres.")
    private String senha;

    @NotNull(message = "cargo é obrigatório.")
    private CargoFuncionario cargo;

    private Boolean ativo;

    @NotNull(message = "unidadeId é obrigatório.")
    private Long unidadeId;

}

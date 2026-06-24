package com.raizesdonordeste.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.raizesdonordeste.backend.entity.Fidelidade;
import com.raizesdonordeste.backend.entity.Pedido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@Data
@Schema(description = "Dados para cadastro e atualizacao de cliente")
public class ClienteDTO {

    @Schema(description = "Identificador do cliente", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "nome é obrigatório.")
    @Schema(description = "Nome completo", example = "Maria Silva")
    private String nome;

    @NotBlank(message = "email é obrigatório.")
    @Email(message = "email deve ser válido.")
    @Schema(description = "E-mail único do cliente", example = "maria@example.com")
    private String email;

    @NotBlank(message = "senha é obrigatória.")
    @Size(min = 8, message = "senha deve ter ao menos 8 caracteres.")
    @Schema(description = "Senha com ao menos oito caracteres", example = "Cliente@123", minLength = 8)
    private String senha;

    @NotBlank(message = "telefone é obrigatório.")
    @Schema(description = "Telefone para contato", example = "(81) 99999-9999")
    private String telefone;

    @NotNull(message = "consentimentoLgpd é obrigatório.")
    @Schema(description = "Consentimento para tratamento de dados pessoais", example = "true")
    private Boolean consentimentoLgpd;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataConsentimento;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Fidelidade fidelidade;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<Pedido> pedidos;
}

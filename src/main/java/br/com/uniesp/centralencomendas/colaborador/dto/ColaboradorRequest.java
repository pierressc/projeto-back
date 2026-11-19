package br.com.uniesp.centralencomendas.colaborador.dto;

import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ColaboradorRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 120) String email,
        @NotNull SetorColaborador setor
) {
}

package br.com.uniesp.centralencomendas.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 25) String telefone,
        @Email @Size(max = 120) String email,
        @Size(max = 500) String observacoes
) {
}

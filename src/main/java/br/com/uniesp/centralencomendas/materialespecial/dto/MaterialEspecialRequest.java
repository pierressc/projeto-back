package br.com.uniesp.centralencomendas.materialespecial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaterialEspecialRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 300) String descricao,
        @NotBlank @Size(max = 30) String unidadeMedida,
        @Size(max = 120) String fornecedorSugerido
) {
}

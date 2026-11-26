package br.com.uniesp.centralencomendas.solicitacaoalteracao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecusarAlteracaoRequest(
        @NotNull Long avaliadorId,
        @NotBlank @Size(max = 400) String motivoResposta
) {
}

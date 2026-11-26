package br.com.uniesp.centralencomendas.entrega.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConcluirEntregaRequest(
        @NotNull Long responsavelEntregaId,
        @Size(max = 400) String observacoes
) {
}

package br.com.uniesp.centralencomendas.colaborador.dto;

import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;
import jakarta.validation.constraints.NotNull;

public record ColaboradorSetorRequest(
        @NotNull SetorColaborador setor
) {
}

package br.com.uniesp.centralencomendas.solicitacaoalteracao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SolicitarAlteracaoRequest(
        @NotNull Long solicitanteId,
        @NotBlank @Size(max = 500) String descricaoMudanca,
        @NotNull @DecimalMin("0.00") BigDecimal custoAdicional
) {
}

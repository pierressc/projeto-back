package br.com.uniesp.centralencomendas.solicitacaoalteracao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SolicitarAlteracaoRequest(
        @NotBlank @Size(max = 500) String descricaoMudanca,
        @NotNull @DecimalMin("0.00") BigDecimal custoAdicionalEstimado,
        @NotNull Long solicitanteId,
        LocalDate dataSolicitacao
) {
}

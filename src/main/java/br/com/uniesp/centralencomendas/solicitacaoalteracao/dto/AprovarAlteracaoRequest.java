package br.com.uniesp.centralencomendas.solicitacaoalteracao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AprovarAlteracaoRequest(
        @NotNull Long avaliadorId,
        @NotNull @DecimalMin("0.00") BigDecimal custoExtraConfirmado,
        @Size(max = 400) String motivoResposta
) {
}

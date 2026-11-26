package br.com.uniesp.centralencomendas.pagamento.dto;

import br.com.uniesp.centralencomendas.shared.enums.FormaPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RegistrarPagamentoRequest(
        @NotNull Long registradoPorId,
        @NotNull @DecimalMin("0.01") BigDecimal valor,
        @NotNull FormaPagamento forma,
        @Size(max = 300) String observacao
) {
}

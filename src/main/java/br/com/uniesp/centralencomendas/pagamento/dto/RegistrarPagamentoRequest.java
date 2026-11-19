package br.com.uniesp.centralencomendas.pagamento.dto;

import br.com.uniesp.centralencomendas.shared.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.shared.enums.TipoPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RegistrarPagamentoRequest(
        @NotNull @DecimalMin("0.01") BigDecimal valor,
        @NotNull TipoPagamento tipo,
        @NotNull FormaPagamento forma,
        @NotNull Long responsavelId,
        @Size(max = 400) String observacao
) {
}

package br.com.uniesp.centralencomendas.itemencomenda.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ItemEncomendaRequest(
        @NotNull Long produtoId,
        @Min(1) int quantidade,
        @DecimalMin("0.01") BigDecimal valorUnitario,
        @NotBlank @Size(max = 500) String personalizacao
) {
}

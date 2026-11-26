package br.com.uniesp.centralencomendas.encomenda.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ItemEncomendaRequest(
        @NotBlank @Size(max = 150) String descricaoProduto,
        @NotBlank @Size(max = 500) String personalizacao,
        @Min(1) int quantidade,
        @NotNull @DecimalMin("0.01") BigDecimal valorUnitario
) {
}

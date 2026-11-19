package br.com.uniesp.centralencomendas.itemmaterialencomenda.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ItemMaterialEncomendaRequest(
        @NotNull Long materialId,
        @NotNull @DecimalMin("0.01") BigDecimal quantidadeNecessaria,
        @Size(max = 300) String observacao
) {
}

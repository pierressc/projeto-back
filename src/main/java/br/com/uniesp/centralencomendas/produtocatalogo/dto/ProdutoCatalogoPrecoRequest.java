package br.com.uniesp.centralencomendas.produtocatalogo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProdutoCatalogoPrecoRequest(
        @NotNull @DecimalMin("0.01") BigDecimal novoPrecoBase
) {
}

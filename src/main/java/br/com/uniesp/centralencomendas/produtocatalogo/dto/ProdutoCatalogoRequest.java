package br.com.uniesp.centralencomendas.produtocatalogo.dto;

import br.com.uniesp.centralencomendas.shared.enums.CategoriaProduto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoCatalogoRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 500) String descricao,
        @NotNull CategoriaProduto categoria,
        @NotNull @DecimalMin("0.01") BigDecimal precoBase,
        @Min(1) int pesoCapacidade,
        List<Long> materiaisEspeciaisIds
) {
}

package br.com.uniesp.centralencomendas.produtocatalogo.dto;

import br.com.uniesp.centralencomendas.shared.enums.CategoriaProduto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProdutoCatalogoAtualizacaoRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 500) String descricao,
        @NotNull CategoriaProduto categoria
) {
}

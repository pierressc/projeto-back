package br.com.uniesp.centralencomendas.produtocatalogo.dto;

import br.com.uniesp.centralencomendas.materialespecial.dto.MaterialEspecialResponse;
import br.com.uniesp.centralencomendas.produtocatalogo.entity.ProdutoCatalogo;
import br.com.uniesp.centralencomendas.shared.enums.CategoriaProduto;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoCatalogoResponse(
        Long id,
        String nome,
        String descricao,
        CategoriaProduto categoria,
        BigDecimal precoBase,
        int pesoCapacidade,
        boolean ativo,
        List<MaterialEspecialResponse> materiaisEspeciais
) {
    public static ProdutoCatalogoResponse from(ProdutoCatalogo produto) {
        return new ProdutoCatalogoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getCategoria(),
                produto.getPrecoBase(),
                produto.getPesoCapacidade(),
                produto.isAtivo(),
                produto.getMateriaisEspeciais().stream().map(MaterialEspecialResponse::from).toList()
        );
    }
}

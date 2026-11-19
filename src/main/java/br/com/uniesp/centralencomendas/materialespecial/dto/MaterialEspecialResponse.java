package br.com.uniesp.centralencomendas.materialespecial.dto;

import br.com.uniesp.centralencomendas.materialespecial.entity.MaterialEspecial;

public record MaterialEspecialResponse(
        Long id,
        String nome,
        String descricao,
        String unidadeMedida,
        String fornecedorSugerido,
        boolean ativo
) {
    public static MaterialEspecialResponse from(MaterialEspecial material) {
        return new MaterialEspecialResponse(
                material.getId(),
                material.getNome(),
                material.getDescricao(),
                material.getUnidadeMedida(),
                material.getFornecedorSugerido(),
                material.isAtivo()
        );
    }
}

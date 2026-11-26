package br.com.uniesp.centralencomendas.itemencomenda.dto;

import br.com.uniesp.centralencomendas.itemencomenda.entity.ItemEncomenda;

import java.math.BigDecimal;

public record ItemEncomendaResponse(
        Long id,
        Long encomendaId,
        Long produtoId,
        String produtoNome,
        int quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal,
        String personalizacao
) {
    public static ItemEncomendaResponse from(ItemEncomenda entity) {
        return new ItemEncomendaResponse(
                entity.getId(),
                entity.getEncomenda().getId(),
                entity.getProduto().getId(),
                entity.getProduto().getNome(),
                entity.getQuantidade(),
                entity.getValorUnitario(),
                entity.calcularSubtotal(),
                entity.getPersonalizacao()
        );
    }
}

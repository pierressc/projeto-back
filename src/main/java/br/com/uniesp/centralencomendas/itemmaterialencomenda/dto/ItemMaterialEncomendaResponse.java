package br.com.uniesp.centralencomendas.itemmaterialencomenda.dto;

import br.com.uniesp.centralencomendas.itemmaterialencomenda.entity.ItemMaterialEncomenda;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemMaterialEncomendaResponse(
        Long id,
        Long encomendaId,
        Long materialId,
        String materialNome,
        String unidadeMedida,
        BigDecimal quantidadeNecessaria,
        boolean comprado,
        LocalDateTime dataConfirmacaoCompra,
        String observacao
) {
    public static ItemMaterialEncomendaResponse from(ItemMaterialEncomenda entity) {
        return new ItemMaterialEncomendaResponse(
                entity.getId(),
                entity.getEncomenda().getId(),
                entity.getMaterial().getId(),
                entity.getMaterial().getNome(),
                entity.getMaterial().getUnidadeMedida(),
                entity.getQuantidadeNecessaria(),
                entity.isComprado(),
                entity.getDataConfirmacaoCompra(),
                entity.getObservacao()
        );
    }
}

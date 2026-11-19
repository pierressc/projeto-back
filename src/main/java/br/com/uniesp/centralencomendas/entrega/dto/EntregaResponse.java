package br.com.uniesp.centralencomendas.entrega.dto;

import br.com.uniesp.centralencomendas.entrega.entity.Entrega;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EntregaResponse(
        Long id,
        Long encomendaId,
        String codigoEncomenda,
        StatusEncomenda statusEncomenda,
        TipoEntrega tipo,
        String enderecoEntrega,
        LocalDate dataPrevista,
        LocalDateTime dataRealizada,
        boolean concluida,
        Long responsavelEntregaId,
        String responsavelEntregaNome,
        String observacoes,
        BigDecimal saldoAReceber
) {
    public static EntregaResponse from(Entrega entity) {
        return new EntregaResponse(
                entity.getId(),
                entity.getEncomenda().getId(),
                entity.getEncomenda().getCodigo(),
                entity.getEncomenda().getStatus(),
                entity.getTipo(),
                entity.getEnderecoEntrega(),
                entity.getDataPrevista(),
                entity.getDataRealizada(),
                entity.isConcluida(),
                entity.getResponsavelEntrega() != null ? entity.getResponsavelEntrega().getId() : null,
                entity.getResponsavelEntrega() != null ? entity.getResponsavelEntrega().getNome() : null,
                entity.getObservacoes(),
                entity.getEncomenda().calcularSaldoAReceber()
        );
    }
}

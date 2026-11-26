package br.com.uniesp.centralencomendas.pagamento.dto;

import br.com.uniesp.centralencomendas.pagamento.entity.Pagamento;
import br.com.uniesp.centralencomendas.shared.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.shared.enums.StatusPagamento;
import br.com.uniesp.centralencomendas.shared.enums.TipoPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponse(
        Long id,
        Long encomendaId,
        String codigoEncomenda,
        Long responsavelId,
        String responsavelNome,
        BigDecimal valor,
        TipoPagamento tipo,
        FormaPagamento forma,
        StatusPagamento status,
        LocalDateTime dataPagamento,
        String observacao
) {
    public static PagamentoResponse from(Pagamento entity) {
        return new PagamentoResponse(
                entity.getId(),
                entity.getEncomenda().getId(),
                entity.getEncomenda().getCodigo(),
                entity.getRegistradoPor().getId(),
                entity.getRegistradoPor().getNome(),
                entity.getValor(),
                entity.getTipo(),
                entity.getForma(),
                entity.getStatus(),
                entity.getDataPagamento(),
                entity.getObservacao()
        );
    }
}

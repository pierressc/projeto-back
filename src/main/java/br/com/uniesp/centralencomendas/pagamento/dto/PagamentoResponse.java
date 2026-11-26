package br.com.uniesp.centralencomendas.pagamento.dto;

import br.com.uniesp.centralencomendas.pagamento.entity.Pagamento;
import br.com.uniesp.centralencomendas.shared.enums.FormaPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponse(
        Long id,
        Long encomendaId,
        Long registradoPorId,
        String registradoPorNome,
        BigDecimal valor,
        FormaPagamento forma,
        LocalDateTime dataPagamento,
        String observacao
) {
    public static PagamentoResponse from(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getEncomenda().getId(),
                pagamento.getRegistradoPor().getId(),
                pagamento.getRegistradoPor().getNome(),
                pagamento.getValor(),
                pagamento.getForma(),
                pagamento.getDataPagamento(),
                pagamento.getObservacao()
        );
    }
}

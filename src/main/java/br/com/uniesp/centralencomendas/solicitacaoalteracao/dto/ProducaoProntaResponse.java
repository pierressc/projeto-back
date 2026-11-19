package br.com.uniesp.centralencomendas.solicitacaoalteracao.dto;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;

import java.time.LocalDateTime;

public record ProducaoProntaResponse(
        Long encomendaId,
        String codigoEncomenda,
        StatusEncomenda status,
        LocalDateTime dataConclusaoProducao
) {
    public static ProducaoProntaResponse from(Encomenda encomenda) {
        return new ProducaoProntaResponse(
                encomenda.getId(),
                encomenda.getCodigo(),
                encomenda.getStatus(),
                encomenda.getDataConclusaoProducao()
        );
    }
}

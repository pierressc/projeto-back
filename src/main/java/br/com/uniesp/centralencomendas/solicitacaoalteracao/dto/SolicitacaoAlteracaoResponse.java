package br.com.uniesp.centralencomendas.solicitacaoalteracao.dto;

import br.com.uniesp.centralencomendas.shared.enums.StatusSolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.entity.SolicitacaoAlteracao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SolicitacaoAlteracaoResponse(
        Long id,
        Long encomendaId,
        Long solicitanteId,
        String solicitanteNome,
        Long avaliadorId,
        String avaliadorNome,
        String descricaoMudanca,
        BigDecimal custoAdicional,
        StatusSolicitacaoAlteracao status,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataResposta,
        String motivoResposta
) {
    public static SolicitacaoAlteracaoResponse from(SolicitacaoAlteracao solicitacao) {
        return new SolicitacaoAlteracaoResponse(
                solicitacao.getId(),
                solicitacao.getEncomenda().getId(),
                solicitacao.getSolicitante().getId(),
                solicitacao.getSolicitante().getNome(),
                solicitacao.getAvaliador() == null ? null : solicitacao.getAvaliador().getId(),
                solicitacao.getAvaliador() == null ? null : solicitacao.getAvaliador().getNome(),
                solicitacao.getDescricaoMudanca(),
                solicitacao.getCustoAdicional(),
                solicitacao.getStatus(),
                solicitacao.getDataSolicitacao(),
                solicitacao.getDataResposta(),
                solicitacao.getMotivoResposta()
        );
    }
}

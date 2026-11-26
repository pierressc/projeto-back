package br.com.uniesp.centralencomendas.solicitacaoalteracao.dto;

import br.com.uniesp.centralencomendas.shared.enums.StatusSolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.entity.SolicitacaoAlteracao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SolicitacaoAlteracaoResponse(
        Long id,
        Long encomendaId,
        String codigoEncomenda,
        Long solicitanteId,
        String solicitanteNome,
        Long avaliadorId,
        String avaliadorNome,
        String descricaoMudanca,
        BigDecimal custoAdicional,
        StatusSolicitacaoAlteracao status,
        boolean dentroDoPrazoSemCusto,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataResposta,
        String motivoResposta,
        BigDecimal valorTotalEncomendaAtualizado
) {
    public static SolicitacaoAlteracaoResponse from(SolicitacaoAlteracao entity) {
        return new SolicitacaoAlteracaoResponse(
                entity.getId(),
                entity.getEncomenda().getId(),
                entity.getEncomenda().getCodigo(),
                entity.getSolicitante().getId(),
                entity.getSolicitante().getNome(),
                entity.getAvaliadorProducao() != null ? entity.getAvaliadorProducao().getId() : null,
                entity.getAvaliadorProducao() != null ? entity.getAvaliadorProducao().getNome() : null,
                entity.getDescricaoMudanca(),
                entity.getCustoAdicional(),
                entity.getStatus(),
                entity.isDentroDoPrazoSemCusto(),
                entity.getDataSolicitacao(),
                entity.getDataResposta(),
                entity.getMotivoResposta(),
                entity.getEncomenda().getValorTotal()
        );
    }
}

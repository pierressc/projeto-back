package br.com.uniesp.centralencomendas.encomenda.dto;

import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;

import java.util.List;
import java.util.Map;

public record PainelAndamentoResponse(
        long totalPedidos,
        Map<StatusEncomenda, Long> totaisPorStatus,
        Map<StatusEncomenda, List<EncomendaResumoResponse>> pedidosPorStatus
) {
}

package br.com.uniesp.centralencomendas.encomenda.dto;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.shared.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EncomendaCadastroResponse(
        Long id,
        String codigo,
        Long clienteId,
        String clienteNome,
        Long atendenteId,
        String atendenteNome,
        Long agendaSemanaId,
        String codigoSemana,
        CanalOrigem canalOrigem,
        StatusEncomenda status,
        BigDecimal valorTotal,
        BigDecimal valorSinalExigido,
        BigDecimal totalPago,
        BigDecimal saldoAReceber,
        boolean materiaisConferidos,
        int prazoAlteracaoSemCustoDias,
        TipoEntrega tipoEntrega,
        LocalDate dataPrazoPrometido,
        LocalDateTime dataPedido,
        LocalDateTime dataLiberacaoProducao,
        String observacoesGerais
) {
    public static EncomendaCadastroResponse from(Encomenda encomenda) {
        return new EncomendaCadastroResponse(
                encomenda.getId(),
                encomenda.getCodigo(),
                encomenda.getCliente().getId(),
                encomenda.getCliente().getNome(),
                encomenda.getAtendente().getId(),
                encomenda.getAtendente().getNome(),
                encomenda.getAgendaSemana().getId(),
                encomenda.getAgendaSemana().getCodigoSemana(),
                encomenda.getCanalOrigem(),
                encomenda.getStatus(),
                encomenda.getValorTotal(),
                encomenda.getValorSinalExigido(),
                encomenda.calcularTotalPago(),
                encomenda.calcularSaldoAReceber(),
                encomenda.isMateriaisConferidos(),
                encomenda.getPrazoAlteracaoSemCustoDias(),
                encomenda.getEntrega().getTipo(),
                encomenda.getDataPrazoPrometido(),
                encomenda.getDataPedido(),
                encomenda.getDataLiberacaoProducao(),
                encomenda.getObservacoesGerais()
        );
    }
}

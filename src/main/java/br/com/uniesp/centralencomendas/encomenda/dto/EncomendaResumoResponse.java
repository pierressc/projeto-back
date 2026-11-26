package br.com.uniesp.centralencomendas.encomenda.dto;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.shared.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EncomendaResumoResponse(
        Long id,
        String codigo,
        Long clienteId,
        String clienteNome,
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
        TipoEntrega tipoEntrega,
        LocalDate dataPrazoPrometido,
        LocalDateTime dataPedido
) {
    public static EncomendaResumoResponse from(Encomenda encomenda) {
        return new EncomendaResumoResponse(
                encomenda.getId(),
                encomenda.getCodigo(),
                encomenda.getCliente().getId(),
                encomenda.getCliente().getNome(),
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
                encomenda.getEntrega().getTipo(),
                encomenda.getDataPrazoPrometido(),
                encomenda.getDataPedido()
        );
    }
}

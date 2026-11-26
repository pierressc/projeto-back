package br.com.uniesp.centralencomendas.pagamento.dto;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;

import java.math.BigDecimal;
import java.util.List;

public record ResumoFinanceiroResponse(
        Long encomendaId,
        String codigoEncomenda,
        BigDecimal valorTotal,
        BigDecimal valorSinalExigido,
        boolean sinalPago,
        BigDecimal totalPago,
        BigDecimal saldoAReceber,
        boolean quitadaTrava04,
        List<PagamentoResponse> pagamentos
) {
    public static ResumoFinanceiroResponse from(Encomenda encomenda) {
        BigDecimal saldo = encomenda.calcularSaldoAReceber();
        return new ResumoFinanceiroResponse(
                encomenda.getId(),
                encomenda.getCodigo(),
                encomenda.getValorTotal(),
                encomenda.getValorSinalExigido(),
                encomenda.sinalEstaPago(),
                encomenda.calcularTotalPago(),
                saldo,
                saldo.compareTo(BigDecimal.ZERO) == 0,
                encomenda.getPagamentos().stream().map(PagamentoResponse::from).toList()
        );
    }
}

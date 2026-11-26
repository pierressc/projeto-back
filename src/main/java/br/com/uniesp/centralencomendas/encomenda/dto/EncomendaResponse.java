package br.com.uniesp.centralencomendas.encomenda.dto;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.entity.ItemEncomenda;
import br.com.uniesp.centralencomendas.encomenda.entity.MaterialEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EncomendaResponse(
        Long id,
        String codigo,
        Long clienteId,
        String clienteNome,
        Long atendenteId,
        String atendenteNome,
        CanalOrigem canalOrigem,
        StatusEncomenda status,
        LocalDateTime dataPedido,
        LocalDate dataPrazoEntrega,
        LocalDateTime dataEntregaRealizada,
        TipoEntrega tipoEntrega,
        String enderecoEntrega,
        BigDecimal valorTotal,
        BigDecimal valorSinalExigido,
        BigDecimal totalPago,
        BigDecimal saldoAReceber,
        String observacoes,
        List<ItemResumo> itens,
        List<MaterialResumo> materiais
) {
    public record ItemResumo(
            Long id,
            String descricaoProduto,
            String personalizacao,
            int quantidade,
            BigDecimal valorUnitario,
            BigDecimal subtotal
    ) {
        static ItemResumo from(ItemEncomenda item) {
            return new ItemResumo(
                    item.getId(),
                    item.getDescricaoProduto(),
                    item.getPersonalizacao(),
                    item.getQuantidade(),
                    item.getValorUnitario(),
                    item.calcularSubtotal()
            );
        }
    }

    public record MaterialResumo(
            Long id,
            String nome,
            String quantidade,
            String observacao,
            boolean comprado
    ) {
        static MaterialResumo from(MaterialEncomenda material) {
            return new MaterialResumo(
                    material.getId(),
                    material.getNome(),
                    material.getQuantidade(),
                    material.getObservacao(),
                    material.isComprado()
            );
        }
    }

    public static EncomendaResponse from(Encomenda encomenda) {
        return new EncomendaResponse(
                encomenda.getId(),
                encomenda.getCodigo(),
                encomenda.getCliente().getId(),
                encomenda.getCliente().getNome(),
                encomenda.getAtendente().getId(),
                encomenda.getAtendente().getNome(),
                encomenda.getCanalOrigem(),
                encomenda.getStatus(),
                encomenda.getDataPedido(),
                encomenda.getDataPrazoEntrega(),
                encomenda.getDataEntregaRealizada(),
                encomenda.getTipoEntrega(),
                encomenda.getEnderecoEntrega(),
                encomenda.getValorTotal(),
                encomenda.getValorSinalExigido(),
                encomenda.calcularTotalPago(),
                encomenda.calcularSaldoAReceber(),
                encomenda.getObservacoes(),
                encomenda.getItens().stream().map(ItemResumo::from).toList(),
                encomenda.getMateriais().stream().map(MaterialResumo::from).toList()
        );
    }
}

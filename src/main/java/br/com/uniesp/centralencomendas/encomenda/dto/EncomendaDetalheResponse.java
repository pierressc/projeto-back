package br.com.uniesp.centralencomendas.encomenda.dto;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.entrega.entity.Entrega;
import br.com.uniesp.centralencomendas.itemencomenda.entity.ItemEncomenda;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.entity.ItemMaterialEncomenda;
import br.com.uniesp.centralencomendas.pagamento.entity.Pagamento;
import br.com.uniesp.centralencomendas.shared.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.shared.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.StatusPagamento;
import br.com.uniesp.centralencomendas.shared.enums.StatusSolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;
import br.com.uniesp.centralencomendas.shared.enums.TipoPagamento;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.entity.SolicitacaoAlteracao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EncomendaDetalheResponse(
        Long id,
        String codigo,
        Long clienteId,
        String clienteNome,
        String clienteTelefone,
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
        boolean sinalPago,
        boolean materiaisConferidos,
        boolean todosMateriaisComprados,
        int prazoAlteracaoSemCustoDias,
        String observacoesGerais,
        LocalDateTime dataPedido,
        LocalDate dataPrazoPrometido,
        LocalDateTime dataLiberacaoProducao,
        LocalDateTime dataConclusaoProducao,
        EntregaDetalheDto entrega,
        List<ItemDetalheDto> itens,
        List<MaterialDetalheDto> materiaisEspeciais,
        List<PagamentoDetalheDto> pagamentos,
        List<SolicitacaoAlteracaoDetalheDto> solicitacoesAlteracao
) {
    public static EncomendaDetalheResponse from(Encomenda encomenda) {
        return new EncomendaDetalheResponse(
                encomenda.getId(),
                encomenda.getCodigo(),
                encomenda.getCliente().getId(),
                encomenda.getCliente().getNome(),
                encomenda.getCliente().getTelefone(),
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
                encomenda.sinalEstaPago(),
                encomenda.isMateriaisConferidos(),
                encomenda.todosMateriaisComprados(),
                encomenda.getPrazoAlteracaoSemCustoDias(),
                encomenda.getObservacoesGerais(),
                encomenda.getDataPedido(),
                encomenda.getDataPrazoPrometido(),
                encomenda.getDataLiberacaoProducao(),
                encomenda.getDataConclusaoProducao(),
                EntregaDetalheDto.from(encomenda.getEntrega()),
                encomenda.getItens().stream().map(ItemDetalheDto::from).toList(),
                encomenda.getMateriaisEspeciais().stream().map(MaterialDetalheDto::from).toList(),
                encomenda.getPagamentos().stream().map(PagamentoDetalheDto::from).toList(),
                encomenda.getSolicitacoesAlteracao().stream().map(SolicitacaoAlteracaoDetalheDto::from).toList()
        );
    }

    public record EntregaDetalheDto(
            Long id,
            TipoEntrega tipo,
            String enderecoEntrega,
            LocalDate dataPrevista,
            LocalDateTime dataRealizada,
            boolean concluida,
            String responsavelEntregaNome,
            String observacoes
    ) {
        public static EntregaDetalheDto from(Entrega entrega) {
            return new EntregaDetalheDto(
                    entrega.getId(),
                    entrega.getTipo(),
                    entrega.getEnderecoEntrega(),
                    entrega.getDataPrevista(),
                    entrega.getDataRealizada(),
                    entrega.isConcluida(),
                    entrega.getResponsavelEntrega() != null ? entrega.getResponsavelEntrega().getNome() : null,
                    entrega.getObservacoes()
            );
        }
    }

    public record ItemDetalheDto(
            Long id,
            Long produtoId,
            String produtoNome,
            int quantidade,
            BigDecimal valorUnitario,
            BigDecimal subtotal,
            String personalizacao
    ) {
        public static ItemDetalheDto from(ItemEncomenda item) {
            return new ItemDetalheDto(
                    item.getId(),
                    item.getProduto().getId(),
                    item.getProduto().getNome(),
                    item.getQuantidade(),
                    item.getValorUnitario(),
                    item.calcularSubtotal(),
                    item.getPersonalizacao()
            );
        }
    }

    public record MaterialDetalheDto(
            Long id,
            Long materialId,
            String materialNome,
            String unidadeMedida,
            BigDecimal quantidadeNecessaria,
            boolean comprado,
            LocalDateTime dataConfirmacaoCompra,
            String observacao
    ) {
        public static MaterialDetalheDto from(ItemMaterialEncomenda material) {
            return new MaterialDetalheDto(
                    material.getId(),
                    material.getMaterial().getId(),
                    material.getMaterial().getNome(),
                    material.getMaterial().getUnidadeMedida(),
                    material.getQuantidadeNecessaria(),
                    material.isComprado(),
                    material.getDataConfirmacaoCompra(),
                    material.getObservacao()
            );
        }
    }

    public record PagamentoDetalheDto(
            Long id,
            BigDecimal valor,
            TipoPagamento tipo,
            FormaPagamento forma,
            StatusPagamento status,
            LocalDateTime dataPagamento,
            String responsavelNome,
            String observacao
    ) {
        public static PagamentoDetalheDto from(Pagamento pagamento) {
            return new PagamentoDetalheDto(
                    pagamento.getId(),
                    pagamento.getValor(),
                    pagamento.getTipo(),
                    pagamento.getForma(),
                    pagamento.getStatus(),
                    pagamento.getDataPagamento(),
                    pagamento.getRegistradoPor().getNome(),
                    pagamento.getObservacao()
            );
        }
    }

    public record SolicitacaoAlteracaoDetalheDto(
            Long id,
            String descricaoMudanca,
            BigDecimal custoAdicional,
            StatusSolicitacaoAlteracao status,
            boolean dentroDoPrazoSemCusto,
            LocalDateTime dataSolicitacao,
            String solicitanteNome,
            String avaliadorNome,
            String motivoResposta
    ) {
        public static SolicitacaoAlteracaoDetalheDto from(SolicitacaoAlteracao solicitacao) {
            return new SolicitacaoAlteracaoDetalheDto(
                    solicitacao.getId(),
                    solicitacao.getDescricaoMudanca(),
                    solicitacao.getCustoAdicional(),
                    solicitacao.getStatus(),
                    solicitacao.isDentroDoPrazoSemCusto(),
                    solicitacao.getDataSolicitacao(),
                    solicitacao.getSolicitante().getNome(),
                    solicitacao.getAvaliadorProducao() != null ? solicitacao.getAvaliadorProducao().getNome() : null,
                    solicitacao.getMotivoResposta()
            );
        }
    }
}

package br.com.uniesp.centralencomendas.domain.entity;

import br.com.uniesp.centralencomendas.domain.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.domain.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.domain.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.domain.enums.StatusSolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.domain.enums.TipoEntrega;
import br.com.uniesp.centralencomendas.domain.exception.RegraNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "encomendas")
public class Encomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atendente_id", nullable = false)
    private Colaborador atendente;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origem", nullable = false, length = 20)
    private CanalOrigem canalOrigem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private StatusEncomenda status;

    @Column(name = "data_pedido", nullable = false, updatable = false)
    private LocalDateTime dataPedido;

    @Column(name = "data_prazo_entrega", nullable = false)
    private LocalDate dataPrazoEntrega;

    @Column(name = "data_entrega_realizada")
    private LocalDateTime dataEntregaRealizada;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega", nullable = false, length = 25)
    private TipoEntrega tipoEntrega;

    @Column(name = "endereco_entrega", length = 300)
    private String enderecoEntrega;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_sinal_exigido", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSinalExigido;

    @Column(length = 600)
    private String observacoes;

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemEncomenda> itens = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialEncomenda> materiais = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pagamento> pagamentos = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitacaoAlteracao> solicitacoesAlteracao = new ArrayList<>();

    protected Encomenda() {
    }

    public Encomenda(
            String codigo,
            Cliente cliente,
            Colaborador atendente,
            CanalOrigem canalOrigem,
            LocalDate dataPrazoEntrega,
            TipoEntrega tipoEntrega,
            String enderecoEntrega,
            BigDecimal valorSinalExigido,
            String observacoes
    ) {
        if (codigo == null || codigo.isBlank()) {
            throw new RegraNegocioException("O codigo da encomenda e obrigatorio.");
        }
        if (cliente == null || atendente == null || canalOrigem == null || dataPrazoEntrega == null || tipoEntrega == null) {
            throw new RegraNegocioException("Dados obrigatorios da encomenda nao informados.");
        }
        if (tipoEntrega == TipoEntrega.ENTREGA_DOMICILIO && (enderecoEntrega == null || enderecoEntrega.isBlank())) {
            throw new RegraNegocioException("O endereco e obrigatorio para entregas em domicilio.");
        }
        if (valorSinalExigido == null || valorSinalExigido.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("O valor de sinal exigido nao pode ser negativo.");
        }

        this.codigo = codigo.trim();
        this.cliente = cliente;
        this.atendente = atendente;
        this.canalOrigem = canalOrigem;
        this.dataPrazoEntrega = dataPrazoEntrega;
        this.tipoEntrega = tipoEntrega;
        this.enderecoEntrega = enderecoEntrega == null ? null : enderecoEntrega.trim();
        this.valorSinalExigido = valorSinalExigido;
        this.observacoes = observacoes == null ? null : observacoes.trim();
        this.status = StatusEncomenda.AGUARDANDO_SINAL_MATERIAL;
        this.valorTotal = BigDecimal.ZERO;
        this.dataPedido = LocalDateTime.now();

        cliente.vincularEncomenda(this);
    }

    public ItemEncomenda adicionarItem(
            String descricaoProduto,
            String personalizacao,
            int quantidade,
            BigDecimal valorUnitario
    ) {
        if (this.status != StatusEncomenda.AGUARDANDO_SINAL_MATERIAL) {
            throw new RegraNegocioException("Nao e permitido alterar itens diretamente apos iniciar a producao.");
        }
        ItemEncomenda item = new ItemEncomenda(this, descricaoProduto, personalizacao, quantidade, valorUnitario);
        this.itens.add(item);
        recalcularValorTotal();
        return item;
    }

    public void removerItem(ItemEncomenda item) {
        if (this.status != StatusEncomenda.AGUARDANDO_SINAL_MATERIAL) {
            throw new RegraNegocioException("Nao e permitido remover itens diretamente apos iniciar a producao.");
        }
        if (!this.itens.remove(item)) {
            throw new RegraNegocioException("Item nao encontrado nesta encomenda.");
        }
        recalcularValorTotal();
    }

    public MaterialEncomenda adicionarMaterial(String nome, String quantidade, String observacao) {
        if (this.status == StatusEncomenda.ENTREGUE || this.status == StatusEncomenda.CANCELADO) {
            throw new RegraNegocioException("Nao e possivel adicionar materiais em encomenda finalizada ou cancelada.");
        }
        MaterialEncomenda material = new MaterialEncomenda(this, nome, quantidade, observacao);
        this.materiais.add(material);
        return material;
    }

    public void confirmarCompraMaterial(MaterialEncomenda material) {
        if (!this.materiais.contains(material)) {
            throw new RegraNegocioException("Material nao encontrado nesta encomenda.");
        }
        material.marcarComoComprado();
    }

    public Pagamento registrarPagamento(
            BigDecimal valor,
            FormaPagamento forma,
            Colaborador responsavel,
            String observacao
    ) {
        if (this.status == StatusEncomenda.CANCELADO) {
            throw new RegraNegocioException("Nao e possivel registrar pagamento em encomenda cancelada.");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor do pagamento deve ser maior que zero.");
        }
        BigDecimal saldo = calcularSaldoAReceber();
        if (valor.compareTo(saldo) > 0) {
            throw new RegraNegocioException("O valor informado excede o saldo a receber (" + saldo + ").");
        }
        Pagamento pagamento = new Pagamento(this, responsavel, valor, forma, observacao);
        this.pagamentos.add(pagamento);
        return pagamento;
    }

    public void liberarParaProducao() {
        if (this.status != StatusEncomenda.AGUARDANDO_SINAL_MATERIAL) {
            throw new RegraNegocioException("Apenas encomendas aguardando sinal/material podem ir para producao.");
        }
        if (this.itens.isEmpty()) {
            throw new RegraNegocioException("A encomenda deve possuir ao menos um item.");
        }
        if (!sinalEstaPago()) {
            throw new RegraNegocioException("O pagamento do sinal minimo ainda nao foi confirmado.");
        }
        if (!todosMateriaisComprados()) {
            throw new RegraNegocioException("Existem materiais especiais ainda nao comprados.");
        }
        this.status = StatusEncomenda.EM_PRODUCAO;
    }

    public SolicitacaoAlteracao solicitarAlteracao(
            String descricaoMudanca,
            BigDecimal custoAdicional,
            Colaborador solicitante
    ) {
        if (this.status != StatusEncomenda.EM_PRODUCAO) {
            throw new RegraNegocioException("Solicitacoes de alteracao aplicam-se a encomendas em producao.");
        }
        SolicitacaoAlteracao solicitacao = new SolicitacaoAlteracao(this, solicitante, descricaoMudanca, custoAdicional);
        this.solicitacoesAlteracao.add(solicitacao);
        return solicitacao;
    }

    public void aprovarAlteracao(
            SolicitacaoAlteracao solicitacao,
            Colaborador avaliador,
            BigDecimal custoExtraConfirmado,
            String motivo
    ) {
        if (!this.solicitacoesAlteracao.contains(solicitacao)) {
            throw new RegraNegocioException("Solicitacao de alteracao nao pertence a esta encomenda.");
        }
        solicitacao.aprovar(avaliador, custoExtraConfirmado, motivo);
        recalcularValorTotal();
    }

    public void recusarAlteracao(SolicitacaoAlteracao solicitacao, Colaborador avaliador, String motivo) {
        if (!this.solicitacoesAlteracao.contains(solicitacao)) {
            throw new RegraNegocioException("Solicitacao de alteracao nao pertence a esta encomenda.");
        }
        solicitacao.recusar(avaliador, motivo);
    }

    public void marcarComoPronto() {
        if (this.status != StatusEncomenda.EM_PRODUCAO) {
            throw new RegraNegocioException("Apenas encomendas em producao podem ser marcadas como prontas.");
        }
        boolean existeAlteracaoPendente = this.solicitacoesAlteracao.stream().anyMatch(SolicitacaoAlteracao::isPendente);
        if (existeAlteracaoPendente) {
            throw new RegraNegocioException("Ha solicitacoes de alteracao pendentes de resposta da producao.");
        }
        this.status = StatusEncomenda.PRONTO;
    }

    public void concluirEntrega() {
        if (this.status != StatusEncomenda.PRONTO) {
            throw new RegraNegocioException("Apenas encomendas prontas podem ser entregues.");
        }
        if (calcularSaldoAReceber().compareTo(BigDecimal.ZERO) > 0) {
            throw new RegraNegocioException("Entrega bloqueada: ha saldo pendente de pagamento.");
        }
        this.status = StatusEncomenda.ENTREGUE;
        this.dataEntregaRealizada = LocalDateTime.now();
    }

    public void cancelar(String motivo) {
        if (this.status == StatusEncomenda.ENTREGUE || this.status == StatusEncomenda.CANCELADO) {
            throw new RegraNegocioException("Encomenda ja entregue ou cancelada nao pode ser cancelada.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("O motivo do cancelamento e obrigatorio.");
        }
        this.status = StatusEncomenda.CANCELADO;
        this.observacoes = (this.observacoes == null || this.observacoes.isBlank())
                ? "Cancelada: " + motivo.trim()
                : this.observacoes + " | Cancelada: " + motivo.trim();
    }

    public BigDecimal calcularTotalPago() {
        return pagamentos.stream()
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularSaldoAReceber() {
        BigDecimal saldo = valorTotal.subtract(calcularTotalPago());
        return saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo;
    }

    public boolean sinalEstaPago() {
        return calcularTotalPago().compareTo(valorSinalExigido) >= 0;
    }

    public boolean todosMateriaisComprados() {
        return materiais.stream().allMatch(MaterialEncomenda::isComprado);
    }

    private void recalcularValorTotal() {
        BigDecimal totalItens = itens.stream()
                .map(ItemEncomenda::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAdicionais = solicitacoesAlteracao.stream()
                .filter(s -> s.getStatus() == StatusSolicitacaoAlteracao.APROVADA)
                .map(SolicitacaoAlteracao::getCustoAdicional)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.valorTotal = totalItens.add(totalAdicionais);
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Colaborador getAtendente() {
        return atendente;
    }

    public CanalOrigem getCanalOrigem() {
        return canalOrigem;
    }

    public StatusEncomenda getStatus() {
        return status;
    }

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public LocalDate getDataPrazoEntrega() {
        return dataPrazoEntrega;
    }

    public LocalDateTime getDataEntregaRealizada() {
        return dataEntregaRealizada;
    }

    public TipoEntrega getTipoEntrega() {
        return tipoEntrega;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public BigDecimal getValorSinalExigido() {
        return valorSinalExigido;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public List<ItemEncomenda> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public List<MaterialEncomenda> getMateriais() {
        return Collections.unmodifiableList(materiais);
    }

    public List<Pagamento> getPagamentos() {
        return Collections.unmodifiableList(pagamentos);
    }

    public List<SolicitacaoAlteracao> getSolicitacoesAlteracao() {
        return Collections.unmodifiableList(solicitacoesAlteracao);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Encomenda other && id != null && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

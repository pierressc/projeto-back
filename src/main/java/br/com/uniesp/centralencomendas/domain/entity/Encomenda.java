package br.com.uniesp.centralencomendas.domain.entity;

import br.com.uniesp.centralencomendas.domain.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.domain.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.domain.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.domain.enums.StatusSolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.domain.enums.TipoEntrega;
import br.com.uniesp.centralencomendas.domain.enums.TipoPagamento;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Raiz de agregado principal do sistema. Centraliza itens, materiais especiais,
 * pagamentos parciais, alteracoes protegidas e entrega, aplicando as 4 Travas de Seguranca.
 */
@Entity
@Table(name = "encomendas")
public class Encomenda {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale LOCALE_PT_BR = Locale.of("pt", "BR");

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agenda_semana_id", nullable = false)
    private AgendaSemana agendaSemana;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origem", nullable = false, length = 20)
    private CanalOrigem canalOrigem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private StatusEncomenda status;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_sinal_exigido", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSinalExigido;

    @Column(name = "materiais_conferidos", nullable = false)
    private boolean materiaisConferidos;

    @Column(name = "prazo_alteracao_sem_custo_dias", nullable = false)
    private int prazoAlteracaoSemCustoDias;

    @Column(name = "observacoes_gerais", length = 1000)
    private String observacoesGerais;

    @Column(name = "data_pedido", nullable = false, updatable = false)
    private LocalDateTime dataPedido;

    @Column(name = "data_prazo_prometido", nullable = false)
    private LocalDate dataPrazoPrometido;

    @Column(name = "data_liberacao_producao")
    private LocalDateTime dataLiberacaoProducao;

    @Column(name = "data_conclusao_producao")
    private LocalDateTime dataConclusaoProducao;

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemEncomenda> itens = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemMaterialEncomenda> materiaisEspeciais = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pagamento> pagamentos = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitacaoAlteracao> solicitacoesAlteracao = new ArrayList<>();

    @OneToOne(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private Entrega entrega;

    protected Encomenda() {
    }

    public Encomenda(
            String codigo,
            Cliente cliente,
            Colaborador atendente,
            AgendaSemana agendaSemana,
            CanalOrigem canalOrigem,
            LocalDate dataPrazoPrometido,
            BigDecimal valorSinalExigido,
            int prazoAlteracaoSemCustoDias,
            TipoEntrega tipoEntrega,
            String enderecoEntrega,
            String observacoesGerais
    ) {
        this.codigo = ValidacaoDominio.textoObrigatorio(codigo, "codigo");
        this.cliente = ValidacaoDominio.naoNulo(cliente, "cliente");
        this.atendente = ValidacaoDominio.naoNulo(atendente, "atendente");
        if (!atendente.isAtivo()) {
            throw new RegraNegocioException("Colaborador atendente precisa estar ativo.");
        }
        this.agendaSemana = ValidacaoDominio.naoNulo(agendaSemana, "agendaSemana");
        this.canalOrigem = ValidacaoDominio.naoNulo(canalOrigem, "canalOrigem");
        this.dataPrazoPrometido = ValidacaoDominio.naoNulo(dataPrazoPrometido, "dataPrazoPrometido");
        if (!agendaSemana.cobreData(dataPrazoPrometido)) {
            throw new RegraNegocioException(
                    "A data prometida (" + dataPrazoPrometido + ") nao pertence a semana '" + agendaSemana.getCodigoSemana() + "'."
            );
        }
        this.valorSinalExigido = ValidacaoDominio.valorNaoNegativo(valorSinalExigido, "valorSinalExigido");
        if (prazoAlteracaoSemCustoDias < 0) {
            throw new RegraNegocioException("O prazo de alteracao sem custo nao pode ser negativo.");
        }
        this.prazoAlteracaoSemCustoDias = prazoAlteracaoSemCustoDias;
        this.observacoesGerais = ValidacaoDominio.textoOpcional(observacoesGerais);

        // Cria e valida a entrega antes de reservar vaga na agenda (Trava 01).
        Entrega novaEntrega = new Entrega(this, tipoEntrega, enderecoEntrega, dataPrazoPrometido);
        agendaSemana.reservarVaga();

        this.entrega = novaEntrega;
        this.status = StatusEncomenda.AGUARDANDO_SINAL_MATERIAL;
        this.valorTotal = BigDecimal.ZERO;
        this.materiaisConferidos = false;
        this.dataPedido = LocalDateTime.now();

        cliente.vincularEncomenda(this);
        agendaSemana.vincularEncomenda(this);
    }

    public ItemEncomenda adicionarItem(ProdutoCatalogo produto, int quantidade, BigDecimal valorUnitario, String personalizacao) {
        exigirEdicaoDiretaPermitida("adicionar itens");
        ItemEncomenda item = new ItemEncomenda(this, produto, quantidade, valorUnitario, personalizacao);
        this.itens.add(item);
        recalcularValorTotal();
        return item;
    }

    public void atualizarItem(ItemEncomenda item, int novaQuantidade, BigDecimal novoValorUnitario, String novaPersonalizacao) {
        exigirEdicaoDiretaPermitida("atualizar itens");
        ValidacaoDominio.naoNulo(item, "item");
        if (!this.itens.contains(item)) {
            throw new RegraNegocioException("O item informado nao pertence a esta encomenda.");
        }
        item.atualizarDetalhes(novaQuantidade, novoValorUnitario, novaPersonalizacao);
        recalcularValorTotal();
    }

    public void removerItem(ItemEncomenda item) {
        exigirEdicaoDiretaPermitida("remover itens");
        ValidacaoDominio.naoNulo(item, "item");
        if (!this.itens.remove(item)) {
            throw new RegraNegocioException("O item informado nao pertence a esta encomenda.");
        }
        recalcularValorTotal();
    }

    public ItemMaterialEncomenda adicionarMaterialEspecial(
            MaterialEspecial material,
            BigDecimal quantidadeNecessaria,
            String observacao
    ) {
        if (this.status == StatusEncomenda.ENTREGUE || this.status == StatusEncomenda.CANCELADO) {
            throw new RegraNegocioException("Nao e possivel adicionar materiais a uma encomenda finalizada ou cancelada.");
        }
        ItemMaterialEncomenda itemMaterial = new ItemMaterialEncomenda(this, material, quantidadeNecessaria, observacao);
        this.materiaisEspeciais.add(itemMaterial);
        this.materiaisConferidos = false;
        return itemMaterial;
    }

    public void confirmarCompraMaterial(ItemMaterialEncomenda itemMaterial) {
        ValidacaoDominio.naoNulo(itemMaterial, "itemMaterial");
        if (!this.materiaisEspeciais.contains(itemMaterial)) {
            throw new RegraNegocioException("O material informado nao pertence a esta encomenda.");
        }
        itemMaterial.confirmarCompra();
    }

    public void concluirChecagemMateriais() {
        if (this.status == StatusEncomenda.CANCELADO || this.status == StatusEncomenda.ENTREGUE) {
            throw new RegraNegocioException("Nao e possivel concluir checagem de materiais para encomenda cancelada ou entregue.");
        }
        if (!todosMateriaisComprados()) {
            throw new RegraNegocioException("Existem materiais especiais pendentes de compra nesta encomenda.");
        }
        this.materiaisConferidos = true;
    }

    public Pagamento registrarPagamento(
            BigDecimal valor,
            TipoPagamento tipo,
            FormaPagamento forma,
            Colaborador responsavel,
            String observacao
    ) {
        if (this.status == StatusEncomenda.CANCELADO) {
            throw new RegraNegocioException("Nao e possivel registrar pagamento em encomenda cancelada.");
        }
        ValidacaoDominio.naoNulo(responsavel, "responsavel");
        if (!responsavel.isAtivo()) {
            throw new RegraNegocioException("Colaborador responsavel pelo pagamento precisa estar ativo.");
        }
        ValidacaoDominio.valorPositivo(valor, "valor");

        BigDecimal saldoRestante = calcularSaldoAReceber();
        if (valor.compareTo(saldoRestante) > 0) {
            throw new RegraNegocioException(
                    "O valor do pagamento (" + valor + ") excede o saldo a receber (" + saldoRestante + ")."
            );
        }

        Pagamento pagamento = new Pagamento(this, responsavel, valor, tipo, forma, observacao);
        this.pagamentos.add(pagamento);
        return pagamento;
    }

    public void estornarPagamento(Pagamento pagamento, String motivo) {
        if (this.status == StatusEncomenda.ENTREGUE || this.status == StatusEncomenda.CANCELADO) {
            throw new RegraNegocioException("Nao e possivel estornar pagamento de encomenda entregue ou cancelada.");
        }
        ValidacaoDominio.naoNulo(pagamento, "pagamento");
        if (!this.pagamentos.contains(pagamento)) {
            throw new RegraNegocioException("O pagamento informado nao pertence a esta encomenda.");
        }
        pagamento.estornar(motivo);
    }

    /**
     * Trava 02: libera a encomenda para a fila de producao apenas apos sinal pago e materiais conferidos.
     */
    public void liberarParaProducao() {
        if (this.status != StatusEncomenda.AGUARDANDO_SINAL_MATERIAL) {
            throw new RegraNegocioException(
                    "Somente encomendas aguardando sinal/material podem ser liberadas para producao."
            );
        }
        if (this.itens.isEmpty()) {
            throw new RegraNegocioException("A encomenda deve possuir ao menos um item antes de ir para a producao.");
        }
        if (this.valorSinalExigido.compareTo(this.valorTotal) > 0) {
            throw new RegraNegocioException("O valor do sinal exigido nao pode ser maior que o valor total da encomenda.");
        }
        if (!sinalEstaPago()) {
            throw new RegraNegocioException(
                    "Trava 02: sinal minimo (" + valorSinalExigido + ") ainda nao foi quitado."
            );
        }
        if (!this.materiaisConferidos || !todosMateriaisComprados()) {
            throw new RegraNegocioException(
                    "Trava 02: materiais especiais precisam estar comprados e com checagem concluida."
            );
        }
        this.status = StatusEncomenda.EM_PRODUCAO;
        this.dataLiberacaoProducao = LocalDateTime.now();
    }

    /**
     * Trava 03: apos o inicio da producao, mudancas nao sao editadas diretamente e exigem aprovacao da Producao.
     */
    public SolicitacaoAlteracao solicitarAlteracao(
            String descricaoMudanca,
            BigDecimal custoAdicionalEstimado,
            Colaborador solicitante,
            LocalDate dataSolicitacao
    ) {
        if (this.status != StatusEncomenda.EM_PRODUCAO) {
            throw new RegraNegocioException(
                    "Solicitacoes formais de alteracao aplicam-se a encomendas com status EM_PRODUCAO."
            );
        }
        ValidacaoDominio.naoNulo(solicitante, "solicitante");
        if (!solicitante.isAtivo()) {
            throw new RegraNegocioException("Colaborador solicitante precisa estar ativo.");
        }
        ValidacaoDominio.naoNulo(dataSolicitacao, "dataSolicitacao");

        long diasAteEntrega = ChronoUnit.DAYS.between(dataSolicitacao, this.dataPrazoPrometido);
        boolean dentroDoPrazoSemCusto = diasAteEntrega >= this.prazoAlteracaoSemCustoDias;

        SolicitacaoAlteracao solicitacao = new SolicitacaoAlteracao(
                this,
                solicitante,
                descricaoMudanca,
                custoAdicionalEstimado,
                dentroDoPrazoSemCusto
        );
        this.solicitacoesAlteracao.add(solicitacao);
        return solicitacao;
    }

    public void aprovarSolicitacaoAlteracao(
            SolicitacaoAlteracao solicitacao,
            Colaborador avaliadorProducao,
            BigDecimal custoExtraConfirmado,
            String observacao
    ) {
        ValidacaoDominio.naoNulo(solicitacao, "solicitacao");
        if (!this.solicitacoesAlteracao.contains(solicitacao)) {
            throw new RegraNegocioException("A solicitacao informada nao pertence a esta encomenda.");
        }
        solicitacao.aprovar(avaliadorProducao, custoExtraConfirmado, observacao);
        recalcularValorTotal();
    }

    public void recusarSolicitacaoAlteracao(
            SolicitacaoAlteracao solicitacao,
            Colaborador avaliadorProducao,
            String motivo
    ) {
        ValidacaoDominio.naoNulo(solicitacao, "solicitacao");
        if (!this.solicitacoesAlteracao.contains(solicitacao)) {
            throw new RegraNegocioException("A solicitacao informada nao pertence a esta encomenda.");
        }
        solicitacao.recusar(avaliadorProducao, motivo);
    }

    public void marcarComoPronto() {
        if (this.status != StatusEncomenda.EM_PRODUCAO) {
            throw new RegraNegocioException("Somente encomendas EM_PRODUCAO podem ser marcadas como prontas.");
        }
        boolean existePendenciaAlteracao = this.solicitacoesAlteracao.stream().anyMatch(SolicitacaoAlteracao::isPendente);
        if (existePendenciaAlteracao) {
            throw new RegraNegocioException(
                    "Existem solicitacoes de alteracao pendentes de resposta da Producao antes de concluir."
            );
        }
        this.status = StatusEncomenda.PRONTO;
        this.dataConclusaoProducao = LocalDateTime.now();
    }

    /**
     * Trava 04: bloqueia a conclusao da entrega enquanto houver saldo pendente a receber.
     */
    public void concluirEntrega(Colaborador responsavelEntrega) {
        if (this.status != StatusEncomenda.PRONTO) {
            throw new RegraNegocioException("Somente encomendas com status PRONTO podem ser entregues.");
        }
        BigDecimal saldo = calcularSaldoAReceber();
        if (saldo.compareTo(BigDecimal.ZERO) > 0) {
            throw new RegraNegocioException(
                    "Trava 04: entrega bloqueada pois ainda ha saldo pendente de " + formatarMoeda(saldo) + "."
            );
        }
        this.entrega.registrarConclusao(responsavelEntrega);
        this.status = StatusEncomenda.ENTREGUE;
    }

    public void cancelar(String motivo) {
        if (this.status == StatusEncomenda.ENTREGUE || this.status == StatusEncomenda.CANCELADO) {
            throw new RegraNegocioException("Encomenda ja entregue ou cancelada nao pode ser cancelada.");
        }
        String motivoValidado = ValidacaoDominio.textoObrigatorio(motivo, "motivo");
        this.status = StatusEncomenda.CANCELADO;
        this.observacoesGerais = (this.observacoesGerais == null || this.observacoesGerais.isBlank())
                ? "Cancelada: " + motivoValidado
                : this.observacoesGerais + " | Cancelada: " + motivoValidado;
        this.agendaSemana.liberarVaga();
    }

    public BigDecimal calcularTotalPago() {
        return pagamentos.stream()
                .filter(Pagamento::isConfirmado)
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
        return materiaisEspeciais.stream().allMatch(ItemMaterialEncomenda::isComprado);
    }

    /**
     * Gera resumo pronto para o atendente copiar e enviar ao cliente no WhatsApp.
     */
    public String gerarResumoWhatsApp() {
        StringBuilder sb = new StringBuilder();
        sb.append("*Resumo do Pedido ").append(codigo).append("*\n");
        sb.append("Cliente: ").append(cliente.getNome()).append("\n");
        sb.append("Status: ").append(status.name()).append("\n");
        sb.append("Prazo previsto: ").append(dataPrazoPrometido.format(FORMATO_DATA)).append("\n");
        sb.append("Entrega: ").append(entrega.getTipo().name());
        if (entrega.getTipo() == TipoEntrega.ENTREGA_DOMICILIO && entrega.getEnderecoEntrega() != null) {
            sb.append(" (").append(entrega.getEnderecoEntrega()).append(")");
        }
        sb.append("\n\n*Itens:*\n");
        for (ItemEncomenda item : itens) {
            sb.append("- ").append(item.getQuantidade()).append("x ")
                    .append(item.getProduto().getNome())
                    .append(" (").append(item.getPersonalizacao()).append(") - ")
                    .append(formatarMoeda(item.calcularSubtotal())).append("\n");
        }
        sb.append("\nValor Total: ").append(formatarMoeda(valorTotal)).append("\n");
        sb.append("Total Pago: ").append(formatarMoeda(calcularTotalPago())).append("\n");
        sb.append("Saldo a Receber: ").append(formatarMoeda(calcularSaldoAReceber()));
        return sb.toString();
    }

    private void exigirEdicaoDiretaPermitida(String operacao) {
        if (this.status != StatusEncomenda.AGUARDANDO_SINAL_MATERIAL) {
            throw new RegraNegocioException(
                    "Trava 03: nao e permitido " + operacao + " diretamente quando o status e " + this.status + "."
            );
        }
    }

    private void recalcularValorTotal() {
        BigDecimal totalItens = itens.stream()
                .map(ItemEncomenda::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAdicionaisAprovados = solicitacoesAlteracao.stream()
                .filter(s -> s.getStatus() == StatusSolicitacaoAlteracao.APROVADA)
                .map(SolicitacaoAlteracao::getCustoAdicional)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.valorTotal = totalItens.add(totalAdicionaisAprovados);
    }

    private String formatarMoeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(LOCALE_PT_BR).format(valor);
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

    public AgendaSemana getAgendaSemana() {
        return agendaSemana;
    }

    public CanalOrigem getCanalOrigem() {
        return canalOrigem;
    }

    public StatusEncomenda getStatus() {
        return status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public BigDecimal getValorSinalExigido() {
        return valorSinalExigido;
    }

    public boolean isMateriaisConferidos() {
        return materiaisConferidos;
    }

    public int getPrazoAlteracaoSemCustoDias() {
        return prazoAlteracaoSemCustoDias;
    }

    public String getObservacoesGerais() {
        return observacoesGerais;
    }

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public LocalDate getDataPrazoPrometido() {
        return dataPrazoPrometido;
    }

    public LocalDateTime getDataLiberacaoProducao() {
        return dataLiberacaoProducao;
    }

    public LocalDateTime getDataConclusaoProducao() {
        return dataConclusaoProducao;
    }

    public List<ItemEncomenda> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public List<ItemMaterialEncomenda> getMateriaisEspeciais() {
        return Collections.unmodifiableList(materiaisEspeciais);
    }

    public List<Pagamento> getPagamentos() {
        return Collections.unmodifiableList(pagamentos);
    }

    public List<SolicitacaoAlteracao> getSolicitacoesAlteracao() {
        return Collections.unmodifiableList(solicitacoesAlteracao);
    }

    public Entrega getEntrega() {
        return entrega;
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

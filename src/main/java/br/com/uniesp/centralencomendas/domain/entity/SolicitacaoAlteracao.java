package br.com.uniesp.centralencomendas.domain.entity;

import br.com.uniesp.centralencomendas.domain.enums.StatusSolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.domain.exception.RegraNegocioException;
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
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro formal de pedido de mudanca apos a entrada da encomenda em producao (Trava 03).
 */
@Entity
@Table(name = "solicitacoes_alteracao")
public class SolicitacaoAlteracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Colaborador solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliador_producao_id")
    private Colaborador avaliadorProducao;

    @Column(name = "descricao_mudanca", nullable = false, length = 600)
    private String descricaoMudanca;

    @Column(name = "custo_adicional", nullable = false, precision = 10, scale = 2)
    private BigDecimal custoAdicional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacaoAlteracao status;

    @Column(name = "dentro_do_prazo_sem_custo", nullable = false)
    private boolean dentroDoPrazoSemCusto;

    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_resposta")
    private LocalDateTime dataResposta;

    @Column(name = "motivo_resposta", length = 500)
    private String motivoResposta;

    protected SolicitacaoAlteracao() {
    }

    SolicitacaoAlteracao(
            Encomenda encomenda,
            Colaborador solicitante,
            String descricaoMudanca,
            BigDecimal custoAdicional,
            boolean dentroDoPrazoSemCusto
    ) {
        this.encomenda = ValidacaoDominio.naoNulo(encomenda, "encomenda");
        this.solicitante = ValidacaoDominio.naoNulo(solicitante, "solicitante");
        this.descricaoMudanca = ValidacaoDominio.textoObrigatorio(descricaoMudanca, "descricaoMudanca");
        this.custoAdicional = ValidacaoDominio.valorNaoNegativo(custoAdicional, "custoAdicional");
        this.dentroDoPrazoSemCusto = dentroDoPrazoSemCusto;
        this.status = StatusSolicitacaoAlteracao.PENDENTE;
        this.dataSolicitacao = LocalDateTime.now();
    }

    void aprovar(Colaborador avaliador, BigDecimal custoExtraConfirmado, String motivoResposta) {
        validarDecisaoPendente(avaliador);
        this.custoAdicional = ValidacaoDominio.valorNaoNegativo(custoExtraConfirmado, "custoExtraConfirmado");
        this.avaliadorProducao = avaliador;
        this.status = StatusSolicitacaoAlteracao.APROVADA;
        this.dataResposta = LocalDateTime.now();
        this.motivoResposta = ValidacaoDominio.textoOpcional(motivoResposta);
    }

    void recusar(Colaborador avaliador, String motivoResposta) {
        validarDecisaoPendente(avaliador);
        this.avaliadorProducao = avaliador;
        this.status = StatusSolicitacaoAlteracao.RECUSADA;
        this.dataResposta = LocalDateTime.now();
        this.motivoResposta = ValidacaoDominio.textoObrigatorio(motivoResposta, "motivoResposta");
    }

    private void validarDecisaoPendente(Colaborador avaliador) {
        if (this.status != StatusSolicitacaoAlteracao.PENDENTE) {
            throw new RegraNegocioException("A solicitacao de alteracao ja foi analisada anteriormente.");
        }
        ValidacaoDominio.naoNulo(avaliador, "avaliador");
        if (!avaliador.podeAprovarAlteracaoProducao()) {
            throw new RegraNegocioException(
                    "Apenas colaboradores ativos da Producao ou Administracao podem aprovar ou recusar alteracoes."
            );
        }
    }

    public boolean isPendente() {
        return this.status == StatusSolicitacaoAlteracao.PENDENTE;
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public Colaborador getSolicitante() {
        return solicitante;
    }

    public Colaborador getAvaliadorProducao() {
        return avaliadorProducao;
    }

    public String getDescricaoMudanca() {
        return descricaoMudanca;
    }

    public BigDecimal getCustoAdicional() {
        return custoAdicional;
    }

    public StatusSolicitacaoAlteracao getStatus() {
        return status;
    }

    public boolean isDentroDoPrazoSemCusto() {
        return dentroDoPrazoSemCusto;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public String getMotivoResposta() {
        return motivoResposta;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof SolicitacaoAlteracao other && id != null && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

package br.com.uniesp.centralencomendas.solicitacaoalteracao.entity;

import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.shared.enums.StatusSolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
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
    @JoinColumn(name = "avaliador_id")
    private Colaborador avaliador;

    @Column(name = "descricao_mudanca", nullable = false, length = 500)
    private String descricaoMudanca;

    @Column(name = "custo_adicional", nullable = false, precision = 10, scale = 2)
    private BigDecimal custoAdicional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacaoAlteracao status;

    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_resposta")
    private LocalDateTime dataResposta;

    @Column(name = "motivo_resposta", length = 400)
    private String motivoResposta;

    protected SolicitacaoAlteracao() {
    }

    public SolicitacaoAlteracao(
            Encomenda encomenda,
            Colaborador solicitante,
            String descricaoMudanca,
            BigDecimal custoAdicional
    ) {
        if (encomenda == null || solicitante == null) {
            throw new RegraNegocioException("Encomenda e solicitante sao obrigatorios.");
        }
        if (descricaoMudanca == null || descricaoMudanca.isBlank()) {
            throw new RegraNegocioException("A descricao da mudanca e obrigatoria.");
        }
        if (custoAdicional == null || custoAdicional.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("O custo adicional nao pode ser negativo.");
        }
        this.encomenda = encomenda;
        this.solicitante = solicitante;
        this.descricaoMudanca = descricaoMudanca.trim();
        this.custoAdicional = custoAdicional;
        this.status = StatusSolicitacaoAlteracao.PENDENTE;
        this.dataSolicitacao = LocalDateTime.now();
    }

    public void aprovar(Colaborador avaliador, BigDecimal custoExtraConfirmado, String motivoResposta) {
        validarAvaliacao(avaliador);
        if (custoExtraConfirmado == null || custoExtraConfirmado.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("O custo adicional confirmado nao pode ser negativo.");
        }
        this.avaliador = avaliador;
        this.custoAdicional = custoExtraConfirmado;
        this.status = StatusSolicitacaoAlteracao.APROVADA;
        this.dataResposta = LocalDateTime.now();
        this.motivoResposta = motivoResposta == null ? null : motivoResposta.trim();
    }

    public void recusar(Colaborador avaliador, String motivoResposta) {
        validarAvaliacao(avaliador);
        if (motivoResposta == null || motivoResposta.isBlank()) {
            throw new RegraNegocioException("O motivo da recusa e obrigatorio.");
        }
        this.avaliador = avaliador;
        this.status = StatusSolicitacaoAlteracao.RECUSADA;
        this.dataResposta = LocalDateTime.now();
        this.motivoResposta = motivoResposta.trim();
    }

    private void validarAvaliacao(Colaborador avaliador) {
        if (this.status != StatusSolicitacaoAlteracao.PENDENTE) {
            throw new RegraNegocioException("Esta solicitacao de alteracao ja foi avaliada.");
        }
        if (avaliador == null) {
            throw new RegraNegocioException("O colaborador avaliador e obrigatorio.");
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

    public Colaborador getAvaliador() {
        return avaliador;
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

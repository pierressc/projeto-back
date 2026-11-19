package br.com.uniesp.centralencomendas.pagamento.entity;

import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.shared.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.shared.enums.StatusPagamento;
import br.com.uniesp.centralencomendas.shared.enums.TipoPagamento;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import br.com.uniesp.centralencomendas.shared.validation.ValidacaoDominio;
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
 * Registro financeiro parcial ou total vinculado a uma encomenda (Trava 04).
 */
@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Colaborador registradoPor;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoPagamento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private FormaPagamento forma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status;

    @Column(name = "data_pagamento", nullable = false, updatable = false)
    private LocalDateTime dataPagamento;

    @Column(length = 300)
    private String observacao;

    protected Pagamento() {
    }

    public Pagamento(
            Encomenda encomenda,
            Colaborador registradoPor,
            BigDecimal valor,
            TipoPagamento tipo,
            FormaPagamento forma,
            String observacao
    ) {
        this.encomenda = ValidacaoDominio.naoNulo(encomenda, "encomenda");
        this.registradoPor = ValidacaoDominio.naoNulo(registradoPor, "registradoPor");
        this.valor = ValidacaoDominio.valorPositivo(valor, "valor");
        this.tipo = ValidacaoDominio.naoNulo(tipo, "tipo");
        this.forma = ValidacaoDominio.naoNulo(forma, "forma");
        this.observacao = ValidacaoDominio.textoOpcional(observacao);
        this.status = StatusPagamento.CONFIRMADO;
        this.dataPagamento = LocalDateTime.now();
    }

    public void estornar(String motivo) {
        if (this.status == StatusPagamento.ESTORNADO) {
            throw new RegraNegocioException("Este pagamento ja foi estornado.");
        }
        String motivoValidado = ValidacaoDominio.textoObrigatorio(motivo, "motivo");
        this.status = StatusPagamento.ESTORNADO;
        this.observacao = (this.observacao == null || this.observacao.isBlank())
                ? "Estornado: " + motivoValidado
                : this.observacao + " | Estornado: " + motivoValidado;
    }

    public boolean isConfirmado() {
        return this.status == StatusPagamento.CONFIRMADO;
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public Colaborador getRegistradoPor() {
        return registradoPor;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public TipoPagamento getTipo() {
        return tipo;
    }

    public FormaPagamento getForma() {
        return forma;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public String getObservacao() {
        return observacao;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Pagamento other && id != null && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

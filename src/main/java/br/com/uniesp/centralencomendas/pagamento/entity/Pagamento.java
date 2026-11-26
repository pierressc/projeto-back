package br.com.uniesp.centralencomendas.pagamento.entity;

import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.shared.enums.FormaPagamento;
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
    private FormaPagamento forma;

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
            FormaPagamento forma,
            String observacao
    ) {
        if (encomenda == null || registradoPor == null || forma == null) {
            throw new RegraNegocioException("Encomenda, colaborador responsavel e forma de pagamento sao obrigatorios.");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor do pagamento deve ser maior que zero.");
        }
        this.encomenda = encomenda;
        this.registradoPor = registradoPor;
        this.valor = valor;
        this.forma = forma;
        this.observacao = observacao == null ? null : observacao.trim();
        this.dataPagamento = LocalDateTime.now();
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

    public FormaPagamento getForma() {
        return forma;
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

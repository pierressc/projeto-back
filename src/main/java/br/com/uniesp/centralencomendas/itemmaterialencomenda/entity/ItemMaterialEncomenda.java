package br.com.uniesp.centralencomendas.itemmaterialencomenda.entity;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.materialespecial.entity.MaterialEspecial;
import br.com.uniesp.centralencomendas.shared.validation.ValidacaoDominio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Checagem individual de compra/disponibilidade de um material especial em uma encomenda (Trava 02).
 */
@Entity
@Table(name = "itens_material_encomenda")
public class ItemMaterialEncomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_especial_id", nullable = false)
    private MaterialEspecial material;

    @Column(name = "quantidade_necessaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidadeNecessaria;

    @Column(nullable = false)
    private boolean comprado;

    @Column(name = "data_confirmacao_compra")
    private LocalDateTime dataConfirmacaoCompra;

    @Column(length = 300)
    private String observacao;

    protected ItemMaterialEncomenda() {
    }

    public ItemMaterialEncomenda(Encomenda encomenda, MaterialEspecial material, BigDecimal quantidadeNecessaria, String observacao) {
        this.encomenda = ValidacaoDominio.naoNulo(encomenda, "encomenda");
        this.material = ValidacaoDominio.naoNulo(material, "material");
        this.quantidadeNecessaria = ValidacaoDominio.valorPositivo(quantidadeNecessaria, "quantidadeNecessaria");
        this.observacao = ValidacaoDominio.textoOpcional(observacao);
        this.comprado = false;
    }

    public void confirmarCompra() {
        this.comprado = true;
        this.dataConfirmacaoCompra = LocalDateTime.now();
    }

    public void estornarConfirmacao() {
        this.comprado = false;
        this.dataConfirmacaoCompra = null;
    }

    public void registrarObservacao(String observacao) {
        this.observacao = ValidacaoDominio.textoOpcional(observacao);
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public MaterialEspecial getMaterial() {
        return material;
    }

    public BigDecimal getQuantidadeNecessaria() {
        return quantidadeNecessaria;
    }

    public boolean isComprado() {
        return comprado;
    }

    public LocalDateTime getDataConfirmacaoCompra() {
        return dataConfirmacaoCompra;
    }

    public String getObservacao() {
        return observacao;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ItemMaterialEncomenda other && id != null && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

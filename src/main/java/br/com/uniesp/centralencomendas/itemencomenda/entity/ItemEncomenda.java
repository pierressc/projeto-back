package br.com.uniesp.centralencomendas.itemencomenda.entity;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.produtocatalogo.entity.ProdutoCatalogo;
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
import java.util.Objects;

/**
 * Item personalizado pertencente a uma encomenda especifica.
 */
@Entity
@Table(name = "itens_encomenda")
public class ItemEncomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoCatalogo produto;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    @Column(nullable = false, length = 500)
    private String personalizacao;

    protected ItemEncomenda() {
    }

    public ItemEncomenda(Encomenda encomenda, ProdutoCatalogo produto, int quantidade, BigDecimal valorUnitario, String personalizacao) {
        this.encomenda = ValidacaoDominio.naoNulo(encomenda, "encomenda");
        this.produto = ValidacaoDominio.naoNulo(produto, "produto");
        this.quantidade = ValidacaoDominio.inteiroPositivo(quantidade, "quantidade");
        this.valorUnitario = ValidacaoDominio.valorPositivo(valorUnitario, "valorUnitario");
        this.personalizacao = ValidacaoDominio.textoObrigatorio(personalizacao, "personalizacao");
    }

    public void atualizarDetalhes(int novaQuantidade, BigDecimal novoValorUnitario, String novaPersonalizacao) {
        this.quantidade = ValidacaoDominio.inteiroPositivo(novaQuantidade, "novaQuantidade");
        this.valorUnitario = ValidacaoDominio.valorPositivo(novoValorUnitario, "novoValorUnitario");
        this.personalizacao = ValidacaoDominio.textoObrigatorio(novaPersonalizacao, "novaPersonalizacao");
    }

    public BigDecimal calcularSubtotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public ProdutoCatalogo getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public String getPersonalizacao() {
        return personalizacao;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ItemEncomenda other && id != null && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

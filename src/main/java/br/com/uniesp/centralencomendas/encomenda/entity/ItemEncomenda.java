package br.com.uniesp.centralencomendas.encomenda.entity;

import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
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

@Entity
@Table(name = "itens_encomenda")
public class ItemEncomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @Column(name = "descricao_produto", nullable = false, length = 150)
    private String descricaoProduto;

    @Column(nullable = false, length = 500)
    private String personalizacao;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    protected ItemEncomenda() {
    }

    ItemEncomenda(
            Encomenda encomenda,
            String descricaoProduto,
            String personalizacao,
            int quantidade,
            BigDecimal valorUnitario
    ) {
        if (encomenda == null) {
            throw new RegraNegocioException("A encomenda do item e obrigatoria.");
        }
        if (descricaoProduto == null || descricaoProduto.isBlank()) {
            throw new RegraNegocioException("A descricao do produto e obrigatoria.");
        }
        if (personalizacao == null || personalizacao.isBlank()) {
            throw new RegraNegocioException("Os detalhes de personalizacao sao obrigatorios.");
        }
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero.");
        }
        if (valorUnitario == null || valorUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor unitario deve ser maior que zero.");
        }
        this.encomenda = encomenda;
        this.descricaoProduto = descricaoProduto.trim();
        this.personalizacao = personalizacao.trim();
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
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

    public String getDescricaoProduto() {
        return descricaoProduto;
    }

    public String getPersonalizacao() {
        return personalizacao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
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

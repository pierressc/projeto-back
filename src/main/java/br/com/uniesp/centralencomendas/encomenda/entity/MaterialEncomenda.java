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

import java.util.Objects;

@Entity
@Table(name = "materiais_encomenda")
public class MaterialEncomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 60)
    private String quantidade;

    @Column(length = 300)
    private String observacao;

    @Column(nullable = false)
    private boolean comprado;

    protected MaterialEncomenda() {
    }

    MaterialEncomenda(Encomenda encomenda, String nome, String quantidade, String observacao) {
        if (encomenda == null) {
            throw new RegraNegocioException("A encomenda do material e obrigatoria.");
        }
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome do material especial e obrigatorio.");
        }
        if (quantidade == null || quantidade.isBlank()) {
            throw new RegraNegocioException("A quantidade do material e obrigatoria.");
        }
        this.encomenda = encomenda;
        this.nome = nome.trim();
        this.quantidade = quantidade.trim();
        this.observacao = observacao == null ? null : observacao.trim();
        this.comprado = false;
    }

    public void marcarComoComprado() {
        this.comprado = true;
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public String getNome() {
        return nome;
    }

    public String getQuantidade() {
        return quantidade;
    }

    public String getObservacao() {
        return observacao;
    }

    public boolean isComprado() {
        return comprado;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof MaterialEncomenda other && id != null && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

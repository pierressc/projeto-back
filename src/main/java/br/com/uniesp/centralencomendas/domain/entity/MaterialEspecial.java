package br.com.uniesp.centralencomendas.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Insumo especifico nao recorrente cuja disponibilidade precisa ser checada antes da producao (Trava 02).
 */
@Entity
@Table(name = "materiais_especiais")
public class MaterialEspecial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nome;

    @Column(length = 300)
    private String descricao;

    @Column(name = "unidade_medida", nullable = false, length = 30)
    private String unidadeMedida;

    @Column(name = "fornecedor_sugerido", length = 120)
    private String fornecedorSugerido;

    @Column(nullable = false)
    private boolean ativo;

    @ManyToMany(mappedBy = "materiaisEspeciais")
    private Set<ProdutoCatalogo> produtos = new HashSet<>();

    protected MaterialEspecial() {
    }

    public MaterialEspecial(String nome, String descricao, String unidadeMedida, String fornecedorSugerido) {
        this.nome = ValidacaoDominio.textoObrigatorio(nome, "nome");
        this.descricao = ValidacaoDominio.textoOpcional(descricao);
        this.unidadeMedida = ValidacaoDominio.textoObrigatorio(unidadeMedida, "unidadeMedida");
        this.fornecedorSugerido = ValidacaoDominio.textoOpcional(fornecedorSugerido);
        this.ativo = true;
    }

    public void atualizarDados(String nome, String descricao, String unidadeMedida, String fornecedorSugerido) {
        this.nome = ValidacaoDominio.textoObrigatorio(nome, "nome");
        this.descricao = ValidacaoDominio.textoOpcional(descricao);
        this.unidadeMedida = ValidacaoDominio.textoObrigatorio(unidadeMedida, "unidadeMedida");
        this.fornecedorSugerido = ValidacaoDominio.textoOpcional(fornecedorSugerido);
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    void adicionarProduto(ProdutoCatalogo produto) {
        this.produtos.add(produto);
    }

    void removerProduto(ProdutoCatalogo produto) {
        this.produtos.remove(produto);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public String getFornecedorSugerido() {
        return fornecedorSugerido;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Set<ProdutoCatalogo> getProdutos() {
        return Collections.unmodifiableSet(produtos);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof MaterialEspecial other && id != null && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

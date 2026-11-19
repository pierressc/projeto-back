package br.com.uniesp.centralencomendas.produtocatalogo.entity;

import br.com.uniesp.centralencomendas.materialespecial.entity.MaterialEspecial;
import br.com.uniesp.centralencomendas.shared.enums.CategoriaProduto;
import br.com.uniesp.centralencomendas.shared.validation.ValidacaoDominio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Modelo base de produto personalizado disponivel para encomendas.
 */
@Entity
@Table(name = "produtos_catalogo")
public class ProdutoCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaProduto categoria;

    @Column(name = "preco_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoBase;

    @Column(name = "peso_capacidade", nullable = false)
    private int pesoCapacidade;

    @Column(nullable = false)
    private boolean ativo;

    @ManyToMany
    @JoinTable(
            name = "produto_material_especial",
            joinColumns = @JoinColumn(name = "produto_id"),
            inverseJoinColumns = @JoinColumn(name = "material_especial_id")
    )
    private Set<MaterialEspecial> materiaisEspeciais = new HashSet<>();

    protected ProdutoCatalogo() {
    }

    public ProdutoCatalogo(String nome, String descricao, CategoriaProduto categoria, BigDecimal precoBase, int pesoCapacidade) {
        this.nome = ValidacaoDominio.textoObrigatorio(nome, "nome");
        this.descricao = ValidacaoDominio.textoOpcional(descricao);
        this.categoria = ValidacaoDominio.naoNulo(categoria, "categoria");
        this.precoBase = ValidacaoDominio.valorPositivo(precoBase, "precoBase");
        this.pesoCapacidade = ValidacaoDominio.inteiroPositivo(pesoCapacidade, "pesoCapacidade");
        this.ativo = true;
    }

    public void atualizarCatalogo(String nome, String descricao, CategoriaProduto categoria) {
        this.nome = ValidacaoDominio.textoObrigatorio(nome, "nome");
        this.descricao = ValidacaoDominio.textoOpcional(descricao);
        this.categoria = ValidacaoDominio.naoNulo(categoria, "categoria");
    }

    public void reajustarPrecoBase(BigDecimal novoPrecoBase) {
        this.precoBase = ValidacaoDominio.valorPositivo(novoPrecoBase, "novoPrecoBase");
    }

    public void alterarPesoCapacidade(int novoPeso) {
        this.pesoCapacidade = ValidacaoDominio.inteiroPositivo(novoPeso, "novoPeso");
    }

    public void associarMaterialEspecial(MaterialEspecial material) {
        ValidacaoDominio.naoNulo(material, "material");
        if (this.materiaisEspeciais.add(material)) {
            material.adicionarProduto(this);
        }
    }

    public void desvincularMaterialEspecial(MaterialEspecial material) {
        ValidacaoDominio.naoNulo(material, "material");
        if (this.materiaisEspeciais.remove(material)) {
            material.removerProduto(this);
        }
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
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

    public CategoriaProduto getCategoria() {
        return categoria;
    }

    public BigDecimal getPrecoBase() {
        return precoBase;
    }

    public int getPesoCapacidade() {
        return pesoCapacidade;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Set<MaterialEspecial> getMateriaisEspeciais() {
        return Collections.unmodifiableSet(materiaisEspeciais);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ProdutoCatalogo other && id != null && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

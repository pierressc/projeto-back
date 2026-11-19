package br.com.uniesp.centralencomendas.cliente.entity;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.shared.validation.ValidacaoDominio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa o cliente solicitante das encomendas personalizadas.
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 25)
    private String telefone;

    @Column(length = 120)
    private String email;

    @Column(length = 500)
    private String observacoes;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "cliente")
    private List<Encomenda> encomendas = new ArrayList<>();

    protected Cliente() {
    }

    public Cliente(String nome, String telefone, String email, String observacoes) {
        this.nome = ValidacaoDominio.textoObrigatorio(nome, "nome");
        this.telefone = ValidacaoDominio.textoObrigatorio(telefone, "telefone");
        this.email = ValidacaoDominio.textoOpcional(email);
        this.observacoes = ValidacaoDominio.textoOpcional(observacoes);
        this.dataCadastro = LocalDateTime.now();
    }

    public void atualizarContato(String nome, String telefone, String email) {
        this.nome = ValidacaoDominio.textoObrigatorio(nome, "nome");
        this.telefone = ValidacaoDominio.textoObrigatorio(telefone, "telefone");
        this.email = ValidacaoDominio.textoOpcional(email);
    }

    public void registrarObservacao(String observacoes) {
        this.observacoes = ValidacaoDominio.textoOpcional(observacoes);
    }

    public void vincularEncomenda(Encomenda encomenda) {
        ValidacaoDominio.naoNulo(encomenda, "encomenda");
        if (!this.encomendas.contains(encomenda)) {
            this.encomendas.add(encomenda);
        }
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public List<Encomenda> getEncomendas() {
        return Collections.unmodifiableList(encomendas);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Cliente other && id != null && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

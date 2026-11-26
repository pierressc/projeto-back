package br.com.uniesp.centralencomendas.cliente.entity;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    @Column(length = 400)
    private String observacoes;

    @OneToMany(mappedBy = "cliente")
    private List<Encomenda> encomendas = new ArrayList<>();

    protected Cliente() {
    }

    public Cliente(String nome, String telefone, String email, String observacoes) {
        atualizarContato(nome, telefone, email);
        this.observacoes = observacoes == null ? null : observacoes.trim();
    }

    public void atualizarContato(String nome, String telefone, String email) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome do cliente e obrigatorio.");
        }
        if (telefone == null || telefone.isBlank()) {
            throw new RegraNegocioException("O telefone do cliente e obrigatorio.");
        }
        this.nome = nome.trim();
        this.telefone = telefone.trim();
        this.email = email == null ? null : email.trim();
    }

    public void atualizarObservacoes(String observacoes) {
        this.observacoes = observacoes == null ? null : observacoes.trim();
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

package br.com.uniesp.centralencomendas.domain.entity;

import br.com.uniesp.centralencomendas.domain.enums.SetorColaborador;
import br.com.uniesp.centralencomendas.domain.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "colaboradores")
public class Colaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SetorColaborador setor;

    @Column(nullable = false)
    private boolean ativo;

    protected Colaborador() {
    }

    public Colaborador(String nome, String email, SetorColaborador setor) {
        atualizarDados(nome, email);
        alterarSetor(setor);
        this.ativo = true;
    }

    public void atualizarDados(String nome, String email) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome do colaborador e obrigatorio.");
        }
        if (email == null || email.isBlank()) {
            throw new RegraNegocioException("O e-mail do colaborador e obrigatorio.");
        }
        this.nome = nome.trim();
        this.email = email.trim();
    }

    public void alterarSetor(SetorColaborador novoSetor) {
        if (novoSetor == null) {
            throw new RegraNegocioException("O setor do colaborador e obrigatorio.");
        }
        this.setor = novoSetor;
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

    public String getEmail() {
        return email;
    }

    public SetorColaborador getSetor() {
        return setor;
    }

    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Colaborador other && id != null && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

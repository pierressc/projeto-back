package br.com.uniesp.centralencomendas.domain.entity;

import br.com.uniesp.centralencomendas.domain.enums.SetorColaborador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Representa um colaborador da equipe (atendimento, producao, administracao ou entrega).
 */
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
        this.nome = ValidacaoDominio.textoObrigatorio(nome, "nome");
        this.email = ValidacaoDominio.textoObrigatorio(email, "email");
        this.setor = ValidacaoDominio.naoNulo(setor, "setor");
        this.ativo = true;
    }

    public void atualizarDados(String nome, String email) {
        this.nome = ValidacaoDominio.textoObrigatorio(nome, "nome");
        this.email = ValidacaoDominio.textoObrigatorio(email, "email");
    }

    public void alterarSetor(SetorColaborador novoSetor) {
        this.setor = ValidacaoDominio.naoNulo(novoSetor, "novoSetor");
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public boolean podeAprovarAlteracaoProducao() {
        return ativo && (setor == SetorColaborador.PRODUCAO || setor == SetorColaborador.ADMINISTRACAO);
    }

    public boolean podeConcluirEntrega() {
        return ativo && (setor == SetorColaborador.ENTREGA
                || setor == SetorColaborador.ATENDIMENTO
                || setor == SetorColaborador.ADMINISTRACAO);
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

package br.com.uniesp.centralencomendas.entrega.entity;

import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import br.com.uniesp.centralencomendas.shared.validation.ValidacaoDominio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Dados e controle de conclusao da entrega ou retirada da encomenda (Trava 04).
 */
@Entity
@Table(name = "entregas")
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false, unique = true)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_entrega_id")
    private Colaborador responsavelEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoEntrega tipo;

    @Column(name = "endereco_entrega", length = 300)
    private String enderecoEntrega;

    @Column(name = "data_prevista", nullable = false)
    private LocalDate dataPrevista;

    @Column(name = "data_realizada")
    private LocalDateTime dataRealizada;

    @Column(nullable = false)
    private boolean concluida;

    @Column(length = 400)
    private String observacoes;

    protected Entrega() {
    }

    public Entrega(Encomenda encomenda, TipoEntrega tipo, String enderecoEntrega, LocalDate dataPrevista) {
        this.encomenda = ValidacaoDominio.naoNulo(encomenda, "encomenda");
        this.tipo = ValidacaoDominio.naoNulo(tipo, "tipo");
        this.enderecoEntrega = validarEndereco(tipo, enderecoEntrega);
        this.dataPrevista = ValidacaoDominio.naoNulo(dataPrevista, "dataPrevista");
        this.concluida = false;
    }

    public void reagendar(LocalDate novaDataPrevista) {
        if (this.concluida) {
            throw new RegraNegocioException("Nao e possivel reagendar uma entrega ja concluida.");
        }
        this.dataPrevista = ValidacaoDominio.naoNulo(novaDataPrevista, "novaDataPrevista");
    }

    public void alterarModalidade(TipoEntrega novoTipo, String novoEndereco) {
        if (this.concluida) {
            throw new RegraNegocioException("Nao e possivel alterar a modalidade de uma entrega ja concluida.");
        }
        ValidacaoDominio.naoNulo(novoTipo, "novoTipo");
        this.enderecoEntrega = validarEndereco(novoTipo, novoEndereco);
        this.tipo = novoTipo;
    }

    public void registrarObservacao(String observacoes) {
        this.observacoes = ValidacaoDominio.textoOpcional(observacoes);
    }

    public void registrarConclusao(Colaborador responsavel) {
        if (this.concluida) {
            throw new RegraNegocioException("A entrega desta encomenda ja foi concluida.");
        }
        ValidacaoDominio.naoNulo(responsavel, "responsavel");
        if (!responsavel.podeConcluirEntrega()) {
            throw new RegraNegocioException(
                    "Colaborador sem permissao ou inativo para concluir entrega."
            );
        }
        this.responsavelEntrega = responsavel;
        this.concluida = true;
        this.dataRealizada = LocalDateTime.now();
    }

    private String validarEndereco(TipoEntrega tipoEntrega, String endereco) {
        if (tipoEntrega == TipoEntrega.ENTREGA_DOMICILIO) {
            return ValidacaoDominio.textoObrigatorio(endereco, "enderecoEntrega");
        }
        return ValidacaoDominio.textoOpcional(endereco);
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public Colaborador getResponsavelEntrega() {
        return responsavelEntrega;
    }

    public TipoEntrega getTipo() {
        return tipo;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public LocalDateTime getDataRealizada() {
        return dataRealizada;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public String getObservacoes() {
        return observacoes;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Entrega other && id != null && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

package br.com.uniesp.centralencomendas.agendasemana.entity;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import br.com.uniesp.centralencomendas.shared.validation.ValidacaoDominio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Controla a capacidade semanal de entregas para evitar sobrecarga de prazos (Trava 01).
 */
@Entity
@Table(name = "agendas_semana")
public class AgendaSemana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_semana", nullable = false, unique = true, length = 30)
    private String codigoSemana;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "capacidade_maxima_pedidos", nullable = false)
    private int capacidadeMaximaPedidos;

    @Column(name = "pedidos_reservados", nullable = false)
    private int pedidosReservados;

    @OneToMany(mappedBy = "agendaSemana")
    private List<Encomenda> encomendas = new ArrayList<>();

    protected AgendaSemana() {
    }

    public AgendaSemana(String codigoSemana, LocalDate dataInicio, LocalDate dataFim, int capacidadeMaximaPedidos) {
        this.codigoSemana = ValidacaoDominio.textoObrigatorio(codigoSemana, "codigoSemana");
        this.dataInicio = ValidacaoDominio.naoNulo(dataInicio, "dataInicio");
        this.dataFim = ValidacaoDominio.naoNulo(dataFim, "dataFim");
        if (dataFim.isBefore(dataInicio)) {
            throw new RegraNegocioException("A data de fim da semana nao pode ser anterior a data de inicio.");
        }
        this.capacidadeMaximaPedidos = ValidacaoDominio.inteiroPositivo(capacidadeMaximaPedidos, "capacidadeMaximaPedidos");
        this.pedidosReservados = 0;
    }

    public boolean possuiVagaDisponivel() {
        return pedidosReservados < capacidadeMaximaPedidos;
    }

    public int calcularVagasRestantes() {
        return Math.max(0, capacidadeMaximaPedidos - pedidosReservados);
    }

    public boolean cobreData(LocalDate data) {
        ValidacaoDominio.naoNulo(data, "data");
        return !data.isBefore(dataInicio) && !data.isAfter(dataFim);
    }

    public void reservarVaga() {
        if (!possuiVagaDisponivel()) {
            throw new RegraNegocioException(
                    "Capacidade maxima da semana '" + codigoSemana + "' atingida (" + capacidadeMaximaPedidos + " pedidos)."
            );
        }
        this.pedidosReservados++;
    }

    public void liberarVaga() {
        if (this.pedidosReservados > 0) {
            this.pedidosReservados--;
        }
    }

    public void ajustarCapacidadeMaxima(int novaCapacidade) {
        ValidacaoDominio.inteiroPositivo(novaCapacidade, "novaCapacidade");
        if (novaCapacidade < this.pedidosReservados) {
            throw new RegraNegocioException(
                    "A nova capacidade (" + novaCapacidade + ") nao pode ser menor que os pedidos ja reservados (" + this.pedidosReservados + ")."
            );
        }
        this.capacidadeMaximaPedidos = novaCapacidade;
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

    public String getCodigoSemana() {
        return codigoSemana;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public int getCapacidadeMaximaPedidos() {
        return capacidadeMaximaPedidos;
    }

    public int getPedidosReservados() {
        return pedidosReservados;
    }

    public List<Encomenda> getEncomendas() {
        return Collections.unmodifiableList(encomendas);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof AgendaSemana other && id != null && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

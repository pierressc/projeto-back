package br.com.uniesp.centralencomendas.encomenda.repository;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EncomendaRepository extends JpaRepository<Encomenda, Long> {

    Optional<Encomenda> findByCodigoIgnoreCase(String codigo);

    List<Encomenda> findByStatusOrderByDataPrazoPrometidoAsc(StatusEncomenda status);

    List<Encomenda> findByClienteIdOrderByDataPedidoDesc(Long clienteId);

    List<Encomenda> findByAgendaSemanaIdOrderByDataPrazoPrometidoAsc(Long agendaSemanaId);

    List<Encomenda> findByDataPrazoPrometidoBetweenOrderByDataPrazoPrometidoAsc(LocalDate inicio, LocalDate fim);
}

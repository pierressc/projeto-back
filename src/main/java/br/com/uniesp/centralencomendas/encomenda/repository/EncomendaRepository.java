package br.com.uniesp.centralencomendas.encomenda.repository;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EncomendaRepository extends JpaRepository<Encomenda, Long> {

    List<Encomenda> findAllByOrderByDataPrazoEntregaAsc();

    List<Encomenda> findByStatusOrderByDataPrazoEntregaAsc(StatusEncomenda status);

    List<Encomenda> findByDataPrazoEntregaBetweenOrderByDataPrazoEntregaAsc(LocalDate inicio, LocalDate fim);
}

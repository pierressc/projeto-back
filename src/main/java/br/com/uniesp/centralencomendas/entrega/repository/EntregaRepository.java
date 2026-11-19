package br.com.uniesp.centralencomendas.entrega.repository;

import br.com.uniesp.centralencomendas.entrega.entity.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    Optional<Entrega> findByEncomendaId(Long encomendaId);

    List<Entrega> findByDataPrevistaBetweenOrderByDataPrevistaAsc(LocalDate inicio, LocalDate fim);
}

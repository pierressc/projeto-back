package br.com.uniesp.centralencomendas.agendasemana.repository;

import br.com.uniesp.centralencomendas.agendasemana.entity.AgendaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AgendaSemanaRepository extends JpaRepository<AgendaSemana, Long> {

    Optional<AgendaSemana> findByCodigoSemanaIgnoreCase(String codigoSemana);

    @Query("SELECT a FROM AgendaSemana a WHERE :data BETWEEN a.dataInicio AND a.dataFim")
    Optional<AgendaSemana> findByDataCoberta(@Param("data") LocalDate data);

    List<AgendaSemana> findAllByOrderByDataInicioAsc();
}

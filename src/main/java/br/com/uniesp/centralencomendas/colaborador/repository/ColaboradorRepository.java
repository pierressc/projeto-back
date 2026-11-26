package br.com.uniesp.centralencomendas.colaborador.repository;

import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {
}

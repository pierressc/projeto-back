package br.com.uniesp.centralencomendas.colaborador.repository;

import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {

    Optional<Colaborador> findByEmailIgnoreCase(String email);

    List<Colaborador> findBySetor(SetorColaborador setor);

    List<Colaborador> findByAtivoTrue();
}

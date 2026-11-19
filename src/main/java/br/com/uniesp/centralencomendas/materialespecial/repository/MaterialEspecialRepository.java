package br.com.uniesp.centralencomendas.materialespecial.repository;

import br.com.uniesp.centralencomendas.materialespecial.entity.MaterialEspecial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialEspecialRepository extends JpaRepository<MaterialEspecial, Long> {

    Optional<MaterialEspecial> findByNomeIgnoreCase(String nome);

    List<MaterialEspecial> findByAtivoTrue();
}

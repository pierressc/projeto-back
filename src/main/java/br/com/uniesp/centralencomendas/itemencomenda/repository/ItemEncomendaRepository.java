package br.com.uniesp.centralencomendas.itemencomenda.repository;

import br.com.uniesp.centralencomendas.itemencomenda.entity.ItemEncomenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemEncomendaRepository extends JpaRepository<ItemEncomenda, Long> {

    List<ItemEncomenda> findByEncomendaId(Long encomendaId);
}

package br.com.uniesp.centralencomendas.itemmaterialencomenda.repository;

import br.com.uniesp.centralencomendas.itemmaterialencomenda.entity.ItemMaterialEncomenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemMaterialEncomendaRepository extends JpaRepository<ItemMaterialEncomenda, Long> {

    List<ItemMaterialEncomenda> findByEncomendaId(Long encomendaId);

    List<ItemMaterialEncomenda> findByEncomendaIdAndCompradoFalse(Long encomendaId);
}

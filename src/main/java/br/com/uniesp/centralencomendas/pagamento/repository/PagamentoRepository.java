package br.com.uniesp.centralencomendas.pagamento.repository;

import br.com.uniesp.centralencomendas.pagamento.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByEncomendaIdOrderByDataPagamentoAsc(Long encomendaId);
}

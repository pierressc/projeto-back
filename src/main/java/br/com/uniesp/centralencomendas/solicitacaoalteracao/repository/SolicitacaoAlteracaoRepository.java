package br.com.uniesp.centralencomendas.solicitacaoalteracao.repository;

import br.com.uniesp.centralencomendas.solicitacaoalteracao.entity.SolicitacaoAlteracao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoAlteracaoRepository extends JpaRepository<SolicitacaoAlteracao, Long> {

    List<SolicitacaoAlteracao> findByEncomendaIdOrderByDataSolicitacaoAsc(Long encomendaId);
}

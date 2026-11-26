package br.com.uniesp.centralencomendas.solicitacaoalteracao.service;

import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.colaborador.service.ColaboradorService;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.encomenda.service.EncomendaService;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.AvaliarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.SolicitacaoAlteracaoResponse;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.SolicitarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.entity.SolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.repository.SolicitacaoAlteracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SolicitacaoAlteracaoService {

    private final SolicitacaoAlteracaoRepository solicitacaoRepository;
    private final EncomendaRepository encomendaRepository;
    private final EncomendaService encomendaService;
    private final ColaboradorService colaboradorService;

    public SolicitacaoAlteracaoService(
            SolicitacaoAlteracaoRepository solicitacaoRepository,
            EncomendaRepository encomendaRepository,
            EncomendaService encomendaService,
            ColaboradorService colaboradorService
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.encomendaRepository = encomendaRepository;
        this.encomendaService = encomendaService;
        this.colaboradorService = colaboradorService;
    }

    @Transactional
    public SolicitacaoAlteracaoResponse solicitar(Long encomendaId, SolicitarAlteracaoRequest request) {
        Encomenda encomenda = encomendaService.buscarEntidade(encomendaId);
        Colaborador solicitante = colaboradorService.buscarEntidade(request.solicitanteId());

        SolicitacaoAlteracao solicitacao = encomenda.solicitarAlteracao(
                request.descricaoMudanca(),
                request.custoAdicional(),
                solicitante
        );
        return SolicitacaoAlteracaoResponse.from(solicitacaoRepository.save(solicitacao));
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAlteracaoResponse> listarPorEncomenda(Long encomendaId) {
        encomendaService.buscarEntidade(encomendaId);
        return solicitacaoRepository.findByEncomendaIdOrderByDataSolicitacaoAsc(encomendaId)
                .stream()
                .map(SolicitacaoAlteracaoResponse::from)
                .toList();
    }

    @Transactional
    public SolicitacaoAlteracaoResponse aprovar(Long encomendaId, Long alteracaoId, AvaliarAlteracaoRequest request) {
        Encomenda encomenda = encomendaService.buscarEntidade(encomendaId);
        SolicitacaoAlteracao solicitacao = buscarDaEncomenda(encomenda, alteracaoId);
        Colaborador avaliador = colaboradorService.buscarEntidade(request.avaliadorId());

        BigDecimal custoConfirmado = request.custoExtraConfirmado() != null
                ? request.custoExtraConfirmado()
                : solicitacao.getCustoAdicional();

        encomenda.aprovarAlteracao(solicitacao, avaliador, custoConfirmado, request.motivo());
        encomendaRepository.save(encomenda);
        return SolicitacaoAlteracaoResponse.from(solicitacao);
    }

    @Transactional
    public SolicitacaoAlteracaoResponse recusar(Long encomendaId, Long alteracaoId, AvaliarAlteracaoRequest request) {
        Encomenda encomenda = encomendaService.buscarEntidade(encomendaId);
        SolicitacaoAlteracao solicitacao = buscarDaEncomenda(encomenda, alteracaoId);
        Colaborador avaliador = colaboradorService.buscarEntidade(request.avaliadorId());

        encomenda.recusarAlteracao(solicitacao, avaliador, request.motivo());
        encomendaRepository.save(encomenda);
        return SolicitacaoAlteracaoResponse.from(solicitacao);
    }

    private SolicitacaoAlteracao buscarDaEncomenda(Encomenda encomenda, Long alteracaoId) {
        return encomenda.getSolicitacoesAlteracao().stream()
                .filter(s -> s.getId().equals(alteracaoId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitacao de alteracao nao encontrada: " + alteracaoId));
    }
}

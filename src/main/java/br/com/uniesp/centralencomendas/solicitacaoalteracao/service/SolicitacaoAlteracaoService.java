package br.com.uniesp.centralencomendas.solicitacaoalteracao.service;

import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.colaborador.repository.ColaboradorRepository;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.AprovarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.ProducaoProntaResponse;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.RecusarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.SolicitacaoAlteracaoResponse;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.SolicitarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.entity.SolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.repository.SolicitacaoAlteracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SolicitacaoAlteracaoService {

    private final EncomendaRepository encomendaRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final SolicitacaoAlteracaoRepository solicitacaoRepository;

    public SolicitacaoAlteracaoService(
            EncomendaRepository encomendaRepository,
            ColaboradorRepository colaboradorRepository,
            SolicitacaoAlteracaoRepository solicitacaoRepository
    ) {
        this.encomendaRepository = encomendaRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional
    public SolicitacaoAlteracaoResponse solicitar(Long encomendaId, SolicitarAlteracaoRequest request) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        Colaborador solicitante = buscarColaborador(request.solicitanteId(), "solicitante");
        LocalDate dataSolicitacao = request.dataSolicitacao() != null ? request.dataSolicitacao() : LocalDate.now();

        SolicitacaoAlteracao solicitacao = encomenda.solicitarAlteracao(
                request.descricaoMudanca(),
                request.custoAdicionalEstimado(),
                solicitante,
                dataSolicitacao
        );
        encomendaRepository.saveAndFlush(encomenda);
        return SolicitacaoAlteracaoResponse.from(solicitacao);
    }

    @Transactional
    public SolicitacaoAlteracaoResponse aprovar(Long encomendaId, Long solicitacaoId, AprovarAlteracaoRequest request) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        SolicitacaoAlteracao solicitacao = buscarSolicitacao(solicitacaoId);
        Colaborador avaliador = buscarColaborador(request.avaliadorId(), "avaliador");

        encomenda.aprovarSolicitacaoAlteracao(
                solicitacao,
                avaliador,
                request.custoExtraConfirmado(),
                request.motivoResposta()
        );
        encomendaRepository.saveAndFlush(encomenda);
        return SolicitacaoAlteracaoResponse.from(solicitacao);
    }

    @Transactional
    public SolicitacaoAlteracaoResponse recusar(Long encomendaId, Long solicitacaoId, RecusarAlteracaoRequest request) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        SolicitacaoAlteracao solicitacao = buscarSolicitacao(solicitacaoId);
        Colaborador avaliador = buscarColaborador(request.avaliadorId(), "avaliador");

        encomenda.recusarSolicitacaoAlteracao(solicitacao, avaliador, request.motivoResposta());
        encomendaRepository.saveAndFlush(encomenda);
        return SolicitacaoAlteracaoResponse.from(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAlteracaoResponse> listarPorEncomenda(Long encomendaId) {
        buscarEncomenda(encomendaId);
        return solicitacaoRepository.findByEncomendaIdOrderByDataSolicitacaoAsc(encomendaId).stream()
                .map(SolicitacaoAlteracaoResponse::from)
                .toList();
    }

    @Transactional
    public ProducaoProntaResponse marcarEncomendaComoPronta(Long encomendaId) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        encomenda.marcarComoPronto();
        return ProducaoProntaResponse.from(encomendaRepository.saveAndFlush(encomenda));
    }

    private Encomenda buscarEncomenda(Long encomendaId) {
        return encomendaRepository.findById(encomendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda nao encontrada com id " + encomendaId + "."));
    }

    private SolicitacaoAlteracao buscarSolicitacao(Long solicitacaoId) {
        return solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitacao de alteracao nao encontrada com id " + solicitacaoId + "."));
    }

    private Colaborador buscarColaborador(Long colaboradorId, String papel) {
        return colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador " + papel + " nao encontrado com id " + colaboradorId + "."));
    }
}

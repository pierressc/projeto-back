package br.com.uniesp.centralencomendas.entrega.service;

import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.colaborador.repository.ColaboradorRepository;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.entrega.dto.AlterarModalidadeEntregaRequest;
import br.com.uniesp.centralencomendas.entrega.dto.ConcluirEntregaRequest;
import br.com.uniesp.centralencomendas.entrega.dto.EntregaResponse;
import br.com.uniesp.centralencomendas.entrega.dto.ReagendarEntregaRequest;
import br.com.uniesp.centralencomendas.entrega.entity.Entrega;
import br.com.uniesp.centralencomendas.entrega.repository.EntregaRepository;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntregaService {

    private final EncomendaRepository encomendaRepository;
    private final EntregaRepository entregaRepository;
    private final ColaboradorRepository colaboradorRepository;

    public EntregaService(
            EncomendaRepository encomendaRepository,
            EntregaRepository entregaRepository,
            ColaboradorRepository colaboradorRepository
    ) {
        this.encomendaRepository = encomendaRepository;
        this.entregaRepository = entregaRepository;
        this.colaboradorRepository = colaboradorRepository;
    }

    @Transactional(readOnly = true)
    public EntregaResponse consultarPorEncomenda(Long encomendaId) {
        Entrega entrega = entregaRepository.findByEncomendaId(encomendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Entrega nao encontrada para a encomenda " + encomendaId + "."));
        return EntregaResponse.from(entrega);
    }

    @Transactional
    public EntregaResponse reagendar(Long encomendaId, ReagendarEntregaRequest request) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        Entrega entrega = encomenda.getEntrega();
        entrega.reagendar(request.novaDataPrevista());
        encomendaRepository.saveAndFlush(encomenda);
        return EntregaResponse.from(entrega);
    }

    @Transactional
    public EntregaResponse alterarModalidade(Long encomendaId, AlterarModalidadeEntregaRequest request) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        Entrega entrega = encomenda.getEntrega();
        entrega.alterarModalidade(request.novoTipo(), request.novoEndereco());
        encomendaRepository.saveAndFlush(encomenda);
        return EntregaResponse.from(entrega);
    }

    @Transactional
    public EntregaResponse concluir(Long encomendaId, ConcluirEntregaRequest request) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        Colaborador responsavel = colaboradorRepository.findById(request.responsavelEntregaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Colaborador responsavel pela entrega nao encontrado com id " + request.responsavelEntregaId() + "."
                ));
        if (request.observacoes() != null) {
            encomenda.getEntrega().registrarObservacao(request.observacoes());
        }
        encomenda.concluirEntrega(responsavel);
        encomendaRepository.saveAndFlush(encomenda);
        return EntregaResponse.from(encomenda.getEntrega());
    }

    private Encomenda buscarEncomenda(Long encomendaId) {
        return encomendaRepository.findById(encomendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda nao encontrada com id " + encomendaId + "."));
    }
}

package br.com.uniesp.centralencomendas.pagamento.service;

import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.colaborador.repository.ColaboradorRepository;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.pagamento.dto.EstornarPagamentoRequest;
import br.com.uniesp.centralencomendas.pagamento.dto.PagamentoResponse;
import br.com.uniesp.centralencomendas.pagamento.dto.RegistrarPagamentoRequest;
import br.com.uniesp.centralencomendas.pagamento.dto.ResumoFinanceiroResponse;
import br.com.uniesp.centralencomendas.pagamento.entity.Pagamento;
import br.com.uniesp.centralencomendas.pagamento.repository.PagamentoRepository;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PagamentoService {

    private final EncomendaRepository encomendaRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final PagamentoRepository pagamentoRepository;

    public PagamentoService(
            EncomendaRepository encomendaRepository,
            ColaboradorRepository colaboradorRepository,
            PagamentoRepository pagamentoRepository
    ) {
        this.encomendaRepository = encomendaRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    @Transactional
    public PagamentoResponse registrar(Long encomendaId, RegistrarPagamentoRequest request) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        Colaborador responsavel = colaboradorRepository.findById(request.responsavelId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador nao encontrado com id " + request.responsavelId() + "."));

        Pagamento pagamento = encomenda.registrarPagamento(
                request.valor(),
                request.tipo(),
                request.forma(),
                responsavel,
                request.observacao()
        );
        encomendaRepository.saveAndFlush(encomenda);
        return PagamentoResponse.from(pagamento);
    }

    @Transactional
    public PagamentoResponse estornar(Long encomendaId, Long pagamentoId, EstornarPagamentoRequest request) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento nao encontrado com id " + pagamentoId + "."));

        encomenda.estornarPagamento(pagamento, request.motivo());
        encomendaRepository.saveAndFlush(encomenda);
        return PagamentoResponse.from(pagamento);
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listar(Long encomendaId) {
        buscarEncomenda(encomendaId);
        return pagamentoRepository.findByEncomendaIdOrderByDataPagamentoAsc(encomendaId).stream()
                .map(PagamentoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroResponse consultarResumoFinanceiro(Long encomendaId) {
        return ResumoFinanceiroResponse.from(buscarEncomenda(encomendaId));
    }

    private Encomenda buscarEncomenda(Long encomendaId) {
        return encomendaRepository.findById(encomendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda nao encontrada com id " + encomendaId + "."));
    }
}
